import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { CommerceService } from '../../../coeur/services/commerce.service';
import { NotificationService } from '../../../coeur/services/notification.service';
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

  // Filters
  filterStatut: string | null = null;
  filterCategorie: string | null = null;
  filterVille: string | null = null;

  // Pagination
  currentPage = 1;
  pageSize = 10;

  // Modal State
  showSuspendModal = false;
  showRejectModal = false;
  selectedCommerceId: string | null = null;
  selectedCommerce: any = null;
  motifAction = '';
  showDetailModal = false;
  showSuccessModal = false;
  successTitle = '';
  successMessage = '';

  constructor(private readonly commerceService: CommerceService,
              private readonly notificationService: NotificationService) {}

  ngOnInit(): void {
    this.chargerCommerces();
  }

  chargerCommerces(): void {
    this.loading = true;
    // Charger explicitement par statuts pour la modération (EN_ATTENTE_VALIDATION, VALIDE, SUSPENDU)
    forkJoin({
      attente: this.commerceService.getByStatut('EN_ATTENTE_VALIDATION'),
      valide: this.commerceService.getByStatut('VALIDE'),
      suspendu: this.commerceService.getByStatut('SUSPENDU')
    }).subscribe({
      next: (data) => {
        let list = [...(data.attente || []), ...(data.valide || []), ...(data.suspendu || [])];
        // Appliquer filtres simples côté client
        if (this.filterStatut) list = list.filter(c => c.statut === this.filterStatut);
        if (this.filterCategorie) list = list.filter(c => c.categorie && c.categorie.idcategorie === this.filterCategorie);
        const searchVille = this.filterVille;
        if (searchVille) list = list.filter(c => c.ville && c.ville.toLowerCase().includes(searchVille.toLowerCase()));

        this.commerces = list;
        this.loading = false;
        this.currentPage = 1;
      },
      error: (err) => {
        console.error('Erreur chargement commerces:', err);
        this.loading = false;
      }
    });
  }

  get pagedCommerces(): any[] {
    const startIndex = (this.currentPage - 1) * this.pageSize;
    return this.commerces.slice(startIndex, startIndex + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.commerces.length / this.pageSize);
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) this.currentPage++;
  }

  prevPage(): void {
    if (this.currentPage > 1) this.currentPage--;
  }

  valider(id: string): void {
    this.commerceService.valider(id).subscribe({
      next: () => {
        this.chargerCommerces();
        this.notificationService.notifyRefresh();
        this.successTitle = 'Commerce Validé';
        this.successMessage = 'Le commerce a été validé avec succès et est maintenant visible sur la plateforme.';
        this.showSuccessModal = true;
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
        this.closeSuspend();
        this.chargerCommerces();
        this.notificationService.notifyRefresh();
        this.successTitle = 'Commerce Suspendu';
        this.successMessage = 'Le commerce a été suspendu avec succès. Le commerçant a été notifié par email.';
        this.showSuccessModal = true;
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
        this.closeReject();
        this.chargerCommerces();
        this.notificationService.notifyRefresh();
        this.successTitle = 'Inscription Rejetée';
        this.successMessage = "La demande d'inscription a été rejetée avec succès.";
        this.showSuccessModal = true;
      },
      error: (err) => console.error('Erreur lors du rejet:', err)
    });
  }

  reactiver(id: string): void {
    this.commerceService.reactiver(id).subscribe({
      next: () => {
        this.chargerCommerces();
        this.notificationService.notifyRefresh();
        this.successTitle = 'Commerce Réactivé';
        this.successMessage = 'Le commerce a été réactivé avec succès.';
        this.showSuccessModal = true;
      },
      error: (err) => console.error('Erreur lors de la réactivation:', err)
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
