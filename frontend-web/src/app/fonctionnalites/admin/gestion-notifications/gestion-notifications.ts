import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CommerceService } from '../../../coeur/services/commerce.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-gestion-notifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './gestion-notifications.html',
  styleUrl: './gestion-notifications.css',
})
export class GestionNotifications implements OnInit {
  commercesEnAttente: any[] = [];
  loading = true;

  constructor(
    private commerceService: CommerceService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadPendingCommerces();
  }

  loadPendingCommerces(): void {
    this.loading = true;
    this.commerceService.getByStatut('EN_ATTENTE_VALIDATION').subscribe({
      next: (data) => {
        this.commercesEnAttente = data;
        this.loading = false;
      },
      error: (err) => {
        console.error("Erreur de chargement des commerces en attente", err);
        this.loading = false;
      }
    });
  }

  goToValidation(): void {
    this.router.navigate(['/admin/validation']);
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleString('fr-FR', {
      day: '2-digit', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }
}

