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
    // Appel API pour valider (à simuler ou implémenter)
    console.log('Validation du commerce', id);
    this.commercesAttente = this.commercesAttente.filter(c => c.id !== id);
  }

  rejeter(id: string): void {
    console.log('Rejet du commerce', id);
    this.commercesAttente = this.commercesAttente.filter(c => c.id !== id);
  }
}
