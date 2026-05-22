import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../coeur/services/auth.service';
import { AbonnementService } from '../../../coeur/services/abonnement.service';
import { CommerceService } from '../../../coeur/services/commerce.service';
import { forkJoin } from 'rxjs';

/**
 * Composant affichant l'historique et les statistiques des abonnements du commerçant.
 */
@Component({
  selector: 'app-mon-abonnement',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './mon-abonnement.component.html',
  styleUrl: './mon-abonnement.component.css'
})
export class MonAbonnementComponent implements OnInit {
  abonnements: any[] = [];
  commerces: any[] = [];
  availablePlans: any[] = [];
  loading = true;
  error = '';

  // Stats
  stats = {
    basique: 0,
    premium: 0,
    gold: 0,
    totalDepense: 0
  };

  constructor(
    private readonly authService: AuthService,
    private readonly abonnementService: AbonnementService,
    private readonly commerceService: CommerceService,
    public readonly router: Router
  ) {}

  ngOnInit(): void {
    this.chargerDonnees();
  }

  chargerDonnees(): void {
    const user = this.authService.getUser();
    const userId = user?.iduser || user?.id;
    if (!userId) {
      this.error = 'Utilisateur non connecté.';
      this.loading = false;
      return;
    }

    this.loading = true;
    
    // Charger l'historique des abonnements, les commerces et tous les plans de référence en parallèle
    forkJoin({
      history: this.abonnementService.getHistoryByCommercant(userId),
      commerces: this.commerceService.getByCommercant(userId),
      allPlans: this.abonnementService.getAll()
    }).subscribe({
      next: (result) => {
        this.abonnements = result.history;
        this.commerces = result.commerces;

        // Filtrer et dédupliquer les abonnements de référence (REF-...)
        const seenTypes = new Set<string>();
        this.availablePlans = result.allPlans
          .filter(ab => ab.reference && ab.reference.startsWith('REF-'))
          .filter(ab => {
            const key = (ab.type || '').toUpperCase();
            if (seenTypes.has(key)) return false;
            seenTypes.add(key);
            return true;
          })
          .map(ab => ({
            ...ab,
            nomAffiche: ab.nomAffiche || ab.type,
            prixAffiche: ab.montant != null ? Number(ab.montant) : 0
          }))
          .sort((a, b) => a.prixAffiche - b.prixAffiche); // Basique < Premium < Gold

        this.calculerStats();
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement données abonnements:', err);
        this.error = 'Impossible de charger l\'historique des abonnements.';
        this.loading = false;
      }
    });
  }

  calculerStats(): void {
    this.stats = { basique: 0, premium: 0, gold: 0, totalDepense: 0 };
    
    this.abonnements.forEach(abo => {
      // On ne compte dans les stats types que les abonnements ACTIFS (ou tous ? l'utilisateur a dit "nombre d'abonnement basique...")
      // Généralement on veut savoir combien on a de commerces sous chaque plan ACTUELLEMENT.
      if (abo.statut === 'ACTIF') {
        const type = (abo.type || '').toUpperCase();
        if (type === 'BASIQUE') this.stats.basique++;
        else if (type === 'PREMIUM') this.stats.premium++;
        else if (type === 'GOLD') this.stats.gold++;
      }
      
      // Total dépensé (tous les abonnements, même expirés)
      this.stats.totalDepense += (abo.montant || 0);
    });
  }

  getCommerceNom(idCommerce: string): string {
    const c = this.commerces.find(item => item.idcommerce === idCommerce);
    return c ? c.nom : 'Commerce inconnu';
  }

  getStatutClass(statut: string): string {
    if (statut === 'ACTIF') return 'bg-green-100 text-green-700';
    if (statut === 'EXPIRE') return 'bg-gray-100 text-gray-600';
    if (statut === 'SUSPENDU') return 'bg-red-100 text-red-700';
    return 'bg-blue-100 text-blue-700';
  }

  getPlanClass(type: string): string {
    const t = (type || '').toUpperCase();
    if (t === 'GOLD') return 'text-amber-600 font-bold';
    if (t === 'PREMIUM') return 'text-indigo-600 font-bold';
    return 'text-emerald-600 font-bold';
  }

  changerDePlan(): void {
    // Redirige vers la liste des commerces pour qu'il choisisse lequel modifier
    this.router.navigate(['/commercant/commerces']);
  }
}
