import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CommerceService } from '../../../coeur/services/commerce.service';
import { ModalComponent } from '../../../partages/composants/modal/modal.component';

/**
 * Composant pour la modération des commerces par l'admin
 */
@Component({
  selector: 'app-validation-commerces',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalComponent],
  templateUrl: './validation-commerces.component.html',
  styleUrl: './validation-commerces.component.css'
})
export class ValidationCommercesComponent implements OnInit {
  commercesAttente: any[] = [];
  loading = true;

  // Modal State
  showSuspendModal = false;
  selectedCommerceId: string | null = null;
  motifSuspension = '';

  constructor(private readonly commerceService: CommerceService) {}

  ngOnInit(): void {
    this.chargerCommerces();
  }

  chargerCommerces(): void {
    this.commerceService.getByStatut('EN_ATTENTE_VALIDATION').subscribe({
      next: (data) => {
        this.commercesAttente = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement commerces:', err);
        this.loading = false;
      }
    });
  }

  valider(id: string): void {
    this.commerceService.valider(id).subscribe({
      next: () => {
        this.commercesAttente = this.commercesAttente.filter(c => c.idcommerce !== id);
      },
      error: (err) => console.error('Erreur lors de la validation:', err)
    });
  }

  openSuspend(id: string): void {
    this.selectedCommerceId = id;
    this.motifSuspension = '';
    this.showSuspendModal = true;
  }

  closeSuspend(): void {
    this.showSuspendModal = false;
    this.selectedCommerceId = null;
    this.motifSuspension = '';
  }

  confirmerSuspension(): void {
    if (!this.selectedCommerceId || !this.motifSuspension.trim()) return;

    this.commerceService.suspendre(this.selectedCommerceId, this.motifSuspension).subscribe({
      next: () => {
        this.commercesAttente = this.commercesAttente.filter(c => c.idcommerce !== this.selectedCommerceId);
        this.closeSuspend();
      },
      error: (err) => console.error('Erreur lors de la suspension:', err)
    });
  }
}
