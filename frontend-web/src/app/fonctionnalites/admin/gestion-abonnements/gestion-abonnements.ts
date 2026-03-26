import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AbonnementService } from '../../../coeur/services/abonnement.service';

interface TarifForfait {
  type: string;
  label: string;
  description: string;
  icon: string;
  couleur: string;
  prix: number | null;
  idabonnement: string | null;
  enEdition: boolean;
  prixEdition: number | null;
}

@Component({
  selector: 'app-gestion-abonnements',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gestion-abonnements.html',
  styleUrl: './gestion-abonnements.css',
})
export class GestionAbonnements implements OnInit {
  abonnements: any[] = [];
  loading = true;
  saving = false;
  successMessage = '';

  // Plans tarifaires fixes
  forfaits: TarifForfait[] = [
    {
      type: 'BASIQUE',
      label: 'Basique',
      description: 'Accès aux fonctionnalités de base',
      icon: 'M5 3a2 2 0 00-2 2v2a2 2 0 002 2h2a2 2 0 002-2V5a2 2 0 00-2-2H5zM5 11a2 2 0 00-2 2v2a2 2 0 002 2h2a2 2 0 002-2v-2a2 2 0 00-2-2H5zM11 5a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V5zM14 11a1 1 0 011 1v1h1a1 1 0 110 2h-1v1a1 1 0 11-2 0v-1h-1a1 1 0 110-2h1v-1a1 1 0 011-1z',
      couleur: 'blue',
      prix: null,
      idabonnement: null,
      enEdition: false,
      prixEdition: null
    },
    {
      type: 'PREMIUM',
      label: 'Premium',
      description: 'Fonctionnalités avancées + visibilité accrue',
      icon: 'M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z',
      couleur: 'purple',
      prix: null,
      idabonnement: null,
      enEdition: false,
      prixEdition: null
    },
    {
      type: 'GOLD',
      label: 'Gold',
      description: 'Accès complet + priorité absolue',
      icon: 'M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z',
      couleur: 'yellow',
      prix: null,
      idabonnement: null,
      enEdition: false,
      prixEdition: null
    }
  ];

  constructor(private readonly abonnementService: AbonnementService) {}

  ngOnInit(): void {
    this.chargerAbonnements();
  }

  chargerAbonnements(): void {
    this.abonnementService.getAll().subscribe({
      next: (data) => {
        this.abonnements = data;
        this.loading = false;
        this.associerPrixForfaits(data);
      },
      error: (err) => {
        console.error("Erreur chargement abonnements:", err);
        this.loading = false;
      }
    });
  }

  associerPrixForfaits(abonnements: any[]): void {
    this.forfaits.forEach(forfait => {
      const match = abonnements.find(a => a.type === forfait.type);
      if (match) {
        forfait.prix = match.montant;   // le DTO backend utilise 'montant'
        forfait.idabonnement = match.idabonnement;
      }
    });
  }

  editerTarif(forfait: TarifForfait): void {
    forfait.enEdition = true;
    forfait.prixEdition = forfait.prix;
  }

  annulerEdition(forfait: TarifForfait): void {
    forfait.enEdition = false;
    forfait.prixEdition = null;
  }

  sauvegarderTarif(forfait: TarifForfait): void {
    if (forfait.prixEdition === null || forfait.prixEdition === undefined || forfait.prixEdition < 0) return;
    if (!forfait.idabonnement) return; // Sécurité : pas de création depuis cette interface

    this.saving = true;
    this.abonnementService.updatePrixParType(forfait.type, forfait.prixEdition).subscribe({
      next: () => {
        forfait.prix = forfait.prixEdition;
        forfait.enEdition = false;
        this.saving = false;
        this.showSuccess(`Tarif ${forfait.label} mis à jour avec succès !`);
      },
      error: (err: any) => {
        console.error("Erreur mise à jour tarif:", err);
        this.saving = false;
      }
    });
  }

  showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => this.successMessage = '', 3000);
  }

  getColorClasses(couleur: string): { bg: string, text: string, border: string, badge: string } {
    const map: Record<string, { bg: string, text: string, border: string, badge: string }> = {
      blue:   { bg: 'bg-blue-50',   text: 'text-blue-600',   border: 'border-blue-200',   badge: 'bg-blue-100 text-blue-700' },
      purple: { bg: 'bg-purple-50', text: 'text-purple-600', border: 'border-purple-200', badge: 'bg-purple-100 text-purple-700' },
      yellow: { bg: 'bg-amber-50',  text: 'text-amber-600',  border: 'border-amber-200',  badge: 'bg-amber-100 text-amber-700' },
    };
    return map[couleur] || map['blue'];
  }

  // Abonnements actifs = list totale
  getAbonnementsActifs(): any[] {
    return this.abonnements.filter(a => a.statut === 'ACTIF');
  }
}

