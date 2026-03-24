import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../coeur/services/auth.service';

/**
 * Composant d'inscription pour les commerçants
 */
@Component({
  selector: 'app-inscription-commercant',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './inscription-commercant.component.html',
  styleUrl: './inscription-commercant.component.css'
})
export class InscriptionCommercantComponent {
  user = {
    nom: '',
    prenom: '',
    email: '',
    telephone: '',
    password: '',
    confirmPassword: '',
    numeroRegistreCommerce: ''
  };

  error: string = '';
  loading: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  /**
   * Soumission du formulaire d'inscription
   */
  onSubmit(): void {
    if (this.user.password !== this.user.confirmPassword) {
      this.error = 'Les mots de passe ne correspondent pas.';
      return;
    }

    this.loading = true;
    this.error = '';

    const payload = {
      nom: this.user.nom,
      prenom: this.user.prenom,
      email: this.user.email,
      telephone: this.user.telephone,
      password: this.user.password,
      numeroRegistreCommerce: this.user.numeroRegistreCommerce
    };

    this.authService.registerCommercant(payload).subscribe({
      next: () => {
        this.router.navigate(['/connexion'], { queryParams: { registered: 'true' } });
      },
      error: (err) => {
        this.error = "Erreur lors de l'inscription. L'email est peut-être déjà utilisé.";
        this.loading = false;
        console.error(err);
      }
    });
  }
}
