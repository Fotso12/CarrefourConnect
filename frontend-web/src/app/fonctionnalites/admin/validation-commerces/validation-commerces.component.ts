import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
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
  commerces: any[] = [];
  loading = true;

  // Modal State
  showSuspendModal = false;
  showRejectModal = false;
  selectedCommerceId: string | null = null;
  selectedCommerce: any = null;
  motifAction = '';
  showDetailModal = false;

  constructor(private readonly commerceService: CommerceService) {}

  ngOnInit(): void {
    this.chargerCommerces();
  }

  chargerCommerces(): void {
    forkJoin({
      attente: this.commerceService.getByStatut('EN_ATTENTE_VALIDATION'),
      valide: this.commerceService.getByStatut('VALIDE')
    }).subscribe({
      next: (data) => {
        this.commerces = [...data.attente, ...data.valide];
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
        // Au lieu de supprimer on recharge pour passer de "attente" à "valide" (plus simple)
        this.chargerCommerces();
      },
      error: (err) => console.error('Erreur lors de la validation:', err)
    });
  }

  openSuspend(id: string): void {
    this.selectedCommerceId = id;
    this.motifAction = '';
    this.showSuspendModal = true;
  }

  closeSuspend(): void {
    this.showSuspendModal = false;
    this.selectedCommerceId = null;
    this.motifAction = '';
  }

  confirmerSuspension(): void {
    if (!this.selectedCommerceId || !this.motifAction.trim()) return;

    this.commerceService.suspendre(this.selectedCommerceId, this.motifAction).subscribe({
      next: () => {
        this.commerces = this.commerces.filter(c => c.idcommerce !== this.selectedCommerceId);
        this.closeSuspend();
      },
      error: (err) => console.error('Erreur lors de la suspension:', err)
    });
  }

  openReject(id: string): void {
    this.selectedCommerceId = id;
    this.motifAction = '';
    this.showRejectModal = true;
  }

  closeReject(): void {
    this.showRejectModal = false;
    this.selectedCommerceId = null;
    this.motifAction = '';
  }

  confirmerReject(): void {
    if (!this.selectedCommerceId || !this.motifAction.trim()) return;

    this.commerceService.rejeter(this.selectedCommerceId, this.motifAction).subscribe({
      next: () => {
        this.commerces = this.commerces.filter(c => c.idcommerce !== this.selectedCommerceId);
        this.closeReject();
      },
      error: (err) => console.error('Erreur lors du rejet:', err)
    });
  }

  openDetail(commerce: any): void {
    this.selectedCommerce = commerce;
    this.showDetailModal = true;
  }

  closeDetail(): void {
    this.showDetailModal = false;
    this.selectedCommerce = null;
  }
}
