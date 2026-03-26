import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CommerceService } from '../../../coeur/services/commerce.service';
import { AuthService } from '../../../coeur/services/auth.service';

/**
 * Composant pour la liste des commerces d'un commerçant
 */
@Component({
  selector: 'app-gestion-commerce',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './gestion-commerce.component.html',
  styleUrl: './gestion-commerce.component.css'
})
export class GestionCommerceComponent implements OnInit {
  commerces: any[] = [];
  loading = true;
  stats = {
    total: 0,
    actifs: 0,
    vues: 0
  };

  constructor(
    private readonly commerceService: CommerceService,
    private readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    const user = this.authService.getUser();
    if (!user) return;

    this.loading = true;
    this.commerceService.getByCommercant(user.id).subscribe({
      next: (data) => {
        this.commerces = data.map(c => ({ ...c, currentImg: 0 }));
        
        // Calcul des KPI
        this.stats.total = this.commerces.length;
        this.stats.actifs = this.commerces.filter(c => c.statut === 'VALIDE' || c.statut === 'ACTIF').length;
        this.stats.vues = this.commerces.reduce((acc, current) => acc + (current.nombreVues || 0), 0);

        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  nextImage(commerce: any, event: Event): void {
    event.stopPropagation();
    if (commerce.images && commerce.images.length > 1) {
      commerce.currentImg = (commerce.currentImg + 1) % commerce.images.length;
    }
  }

  prevImage(commerce: any, event: Event): void {
    event.stopPropagation();
    if (commerce.images && commerce.images.length > 1) {
      commerce.currentImg = (commerce.currentImg - 1 + commerce.images.length) % commerce.images.length;
    }
  }

  supprimer(commerce: any): void {
    if (confirm(`Voulez-vous vraiment supprimer "${commerce.nom}" ?`)) {
      this.commerceService.delete(commerce.idcommerce).subscribe({
        next: () => {
          this.commerces = this.commerces.filter(c => c.idcommerce !== commerce.idcommerce);
        },
        error: (err) => console.error('Erreur lors de la suppression', err)
      });
    }
  }
}
