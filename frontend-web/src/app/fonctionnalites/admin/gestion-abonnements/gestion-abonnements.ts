import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbonnementService } from '../../../coeur/services/abonnement.service';

@Component({
  selector: 'app-gestion-abonnements',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './gestion-abonnements.html',
  styleUrl: './gestion-abonnements.css',
})
export class GestionAbonnements implements OnInit {
  abonnements: any[] = [];
  loading = true;

  constructor(private readonly abonnementService: AbonnementService) {}

  ngOnInit(): void {
    this.chargerAbonnements();
  }

  chargerAbonnements(): void {
    this.abonnementService.getAll().subscribe({
      next: (data) => {
        this.abonnements = data;
        this.loading = false;
      },
      error: (err) => {
        console.error("Erreur chargement abonnements:", err);
        this.loading = false;
      }
    });
  }

  supprimer(id: string): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet abonnement ?')) {
      this.abonnementService.delete(id).subscribe({
        next: () => {
          this.abonnements = this.abonnements.filter(a => a.idabonnement !== id);
        },
        error: (err) => console.error("Erreur suppression abonnement:", err)
      });
    }
  }

  getStatutLabel(statut: string): string {
    switch (statut) {
      case 'ACTIF': return 'Actif';
      case 'EXPIRE': return 'Expiré';
      case 'ANNULE': return 'Annulé';
      default: return statut || 'Inconnu';
    }
  }
}
