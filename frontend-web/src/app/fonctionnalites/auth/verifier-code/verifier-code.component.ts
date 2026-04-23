import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../coeur/services/auth.service';

@Component({
  selector: 'app-verifier-code',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './verifier-code.component.html',
  styleUrls: ['./verifier-code.component.css']
})
export class VerifierCodeComponent {
  email = '';
  code = '';
  loading = false;
  error = '';

  constructor(private auth: AuthService, private router: Router) {
    const state: any = this.router.getCurrentNavigation()?.extras.state;
    if (state?.email) this.email = state.email;
  }

  verifier() {
    if (!this.email || !this.code) return;
    this.loading = true;
    this.auth.verifyResetCode(this.email, this.code).subscribe({
      next: (res: any) => {
        this.loading = false;
        const token = res?.token;
        // navigate to reset page with token and email
        this.router.navigate(['/auth/reinitialiser-mot-de-passe'], { state: { email: this.email, token } });
      },
      error: (err: any) => {
        this.loading = false;
        this.error = err?.error?.error || 'Code invalide ou expiré.';
      }
    });
  }
}
