import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CommerceService } from '../../../coeur/services/commerce.service';

/**
 * Composant pour la modération des commerces par l'admin
 */
@Component({
  selector: 'app-validation-commerces',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './validation-commerces.component.html',
  styleUrl: './validation-commerces.component.css'
})
export class ValidationCommercesComponent implements OnInit {
  commercesAttente: any[] = [];
  loading = true;

  constructor(private readonly commerceService: CommerceService) {}

  ngOnInit(): void {
    this.chargerCommerces();
  }

  chargerCommerces(): void {
    this.commerceService.getAll().subscribe(data => {
      // Filtrer les commerces en attente de validation
      this.commercesAttente = data.filter(c => c.statut === 'EN_ATTENTE_VALIDATION' || !c.statut);
      this.loading = false;
    });
  }

  valider(id: string): void {
    this.commerceService.updateStatut(id, 'VALIDE').subscribe({
      next: () => {
        this.commercesAttente = this.commercesAttente.filter(c => c.idcommerce !== id);
      },
      error: (err) => console.error('Erreur lors de la validation:', err)
    });
  }

  rejeter(id: string): void {
    if (confirm('Êtes-vous sûr de vouloir rejeter ce commerce ?')) {
      this.commerceService.updateStatut(id, 'REJETE').subscribe({
        next: () => {
          this.commercesAttente = this.commercesAttente.filter(c => c.idcommerce !== id);
        },
        error: (err) => console.error('Erreur lors du rejet:', err)
      });
    }
  }
}
