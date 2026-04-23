import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../coeur/services/auth.service';

@Component({
  selector: 'app-reinitialiser-mot-de-passe',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reinitialiser-mot-de-passe.component.html',
  styleUrls: ['./reinitialiser-mot-de-passe.component.css']
})
export class ReinitialiserMotDePasseComponent {
  email = '';
  token = '';
  nouveau = '';
  confirm = '';
  loading = false;
  error = '';

  constructor(private auth: AuthService, private router: Router) {
    const state: any = this.router.getCurrentNavigation()?.extras.state;
    if (state?.email) this.email = state.email;
    if (state?.token) this.token = state.token;
  }

  submit() {
    if (!this.email || !this.token || !this.nouveau || this.nouveau !== this.confirm) return;
    this.loading = true;
    this.auth.resetPassword(this.email, this.token, this.nouveau).subscribe({
      next: () => {
        this.loading = false;
        // redirect to connexion with success message
        this.router.navigate(['/connexion'], { state: { message: 'Mot de passe réinitialisé.' } });
      },
      error: (err: any) => {
        this.loading = false;
        this.error = err?.error?.error || 'Erreur lors de la réinitialisation.';
      }
    });
  }
}
