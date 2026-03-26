import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CommerceService } from '../../../coeur/services/commerce.service';
import { UtilisateurService } from '../../../coeur/services/utilisateur.service';
import { CategorieService } from '../../../coeur/services/categorie.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-stats-admin',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './stats-admin.component.html'
})
export class StatsAdminComponent implements OnInit {
  stats = {
    totalCommerces: 0,
    commercesEnAttente: 0,
    totalUtilisateurs: 0,
    totalCategories: 0
  };
  loading = true;

  constructor(
    private commerceService: CommerceService,
    private utilisateurService: UtilisateurService,
    private categorieService: CategorieService
  ) {}

  ngOnInit(): void {
    this.chargerStats();
  }

  chargerStats(): void {
    forkJoin({
      commerces: this.commerceService.getAll(),
      utilisateurs: this.utilisateurService.getAll(),
      categories: this.categorieService.getAll()
    }).subscribe({
      next: (data) => {
        this.stats.totalCommerces = data.commerces.length;
        this.stats.commercesEnAttente = data.commerces.filter((c: any) => c.statut === 'EN_ATTENTE_VALIDATION' || !c.statut).length;
        this.stats.totalUtilisateurs = data.utilisateurs.length;
        this.stats.totalCategories = data.categories.length;
        this.loading = false;
      },
      error: (err) => {
        console.error("Erreur chargement stats:", err);
        this.loading = false;
      }
    });
  }
}
