import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../coeur/services/auth.service';

@Component({
  selector: 'app-mot-de-passe-oublie',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mot-de-passe-oublie.component.html',
  styleUrls: ['./mot-de-passe-oublie.component.css']
})
export class MotDePasseOublieComponent {
  email = '';
  loading = false;
  message = '';

  constructor(private auth: AuthService, private router: Router) {}

  demanderCode() {
    if (!this.email) return;
    this.loading = true;
    this.auth.requestPasswordReset(this.email).subscribe({
      next: () => {
        this.loading = false;
        this.message = 'Si l\'email existe, un code a été envoyé.';
        // navigate to verify page
        this.router.navigate(['/auth/verifier-code'], { state: { email: this.email } });
      },
      error: () => {
        this.loading = false;
        this.message = 'Erreur lors de la demande.';
      }
    });
  }
}
