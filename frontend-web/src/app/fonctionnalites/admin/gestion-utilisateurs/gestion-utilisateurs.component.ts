import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UtilisateurService } from '../../../coeur/services/utilisateur.service';

@Component({
  selector: 'app-gestion-utilisateurs',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './gestion-utilisateurs.component.html'
})
export class GestionUtilisateursComponent implements OnInit {
  utilisateurs: any[] = [];
  loading = true;

  constructor(private utilisateurService: UtilisateurService) {}

  ngOnInit(): void {
    this.chargerUtilisateurs();
  }

  chargerUtilisateurs(): void {
    this.utilisateurService.getAll().subscribe({
      next: (data) => {
        this.utilisateurs = data;
        this.loading = false;
      },
      error: (err) => {
        console.error("Erreur chargement utilisateurs:", err);
        this.loading = false;
      }
    });
  }

  supprimer(id: string): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ? Cette action est irréversible.')) {
      this.utilisateurService.delete(id).subscribe({
        next: () => {
          this.utilisateurs = this.utilisateurs.filter(u => u.iduser !== id);
        },
        error: (err) => console.error("Erreur suppression:", err)
      });
    }
  }

  getRoleLabel(role: string): string {
    switch (role) {
      case 'ROLE_ADMIN': return 'Administrateur';
      case 'ROLE_COMMERCANT': return 'Commerçant';
      case 'ROLE_VISITEUR': return 'Visiteur';
      default: return role;
    }
  }
}
