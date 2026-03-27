import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../coeur/services/auth.service';

/**
 * Composant de connexion
 */
@Component({
  selector: 'app-connexion',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './connexion.component.html',
  styleUrl: './connexion.component.css'
})
export class ConnexionComponent {
  credentials = {
    email: '',
    password: ''
  };

  error: string = '';
  loading: boolean = false;
  showSuccessModal: boolean = false;
  showErrorModal: boolean = false;
  successMessage: string = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit(): void {
    this.loading = true;
    this.error = '';

    this.authService.login(this.credentials).subscribe({
      next: (res) => {
        // Afficher modal de succès puis rediriger
        this.successMessage = 'Connexion réussie. Bienvenue !';
        this.showSuccessModal = true;
        this.loading = false;
        // Redirection après courte pause pour laisser le modal s'afficher
        setTimeout(() => {
          this.showSuccessModal = false;
          const role: string = (res.role || res.roles?.[0] || '').toUpperCase();
          console.log('[Connexion] Rôle détecté:', role);
          if (role === 'ADMIN' || role === 'ROLE_ADMIN') {
            console.log('[Connexion] Navigation vers /admin');
            this.router.navigate(['/admin']);
          } else {
            console.log('[Connexion] Navigation vers /commercant');
            this.router.navigate(['/commercant']);
          }
        }, 900);
      },
      error: () => {
        this.error = 'Email ou mot de passe incorrect.';
        this.showErrorModal = true;
        this.loading = false;
      }
    });
  }
}
