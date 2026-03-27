import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CommerceService } from '../../coeur/services/commerce.service';
import { CategorieService } from '../../coeur/services/categorie.service';
import { CarteComponent } from '../../partages/composants/carte/carte.component';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';

/**
 * Composant de la page d'accueil
 * Affiche la carte et la liste des commerces récupérés depuis le backend
 */
@Component({
  selector: 'app-accueil',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, CarteComponent],
  templateUrl: './accueil.component.html',
  styleUrl: './accueil.component.css'
})
export class AccueilComponent implements OnInit {
  commerces: any[] = [];
  categories: any[] = [];
  pointsCarte: any[] = [];
  private searchSubject = new Subject<void>();

  topCommerces: any[] = [];
  // Filtres de recherche
  filtres: any = { nom: '', idCategorie: '', ville: '', rayon: 10 };
  showMapOnMobile = false;

  constructor(
    private commerceService: CommerceService,
    private categorieService: CategorieService
  ) {}

  ngOnInit(): void {
    console.log('[Accueil] Chargement de la page d\'accueil...');
    this.chargerCategories();
    this.rechercher(); // Chargement immédiat
    this.initGeolocalisation();

    // Configuration de la recherche temps réel débouncée
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(() => {
      this.rechercher();
    });
  }

  /**
   * Tente de récupérer la position de l'utilisateur sans forcer le filtrage immédiat
   */
  initGeolocalisation(): void {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          (this.filtres as any).lat = pos.coords.latitude;
          (this.filtres as any).lon = pos.coords.longitude;
          this.rechercher(); // Relancer pour appliquer le filtre de proximité si actif
        }
      );
    }
  }

  /**
   * Charge la liste des catégories pour le filtre
   */
  chargerCategories(): void {
    this.categorieService.getAll().subscribe(data => {
      this.categories = data;
    });
  }

  /**
   * Exécute la recherche avec les filtres actuels
   */
  rechercher(): void {
    this.commerceService.rechercher(this.filtres).subscribe({
      next: (data) => {
        // Initialise l'index de l'image courante pour le carrousel
        this.commerces = data.map(c => ({ ...c, currentImg: 0 }));
        this.actualiserCarte(data);
      },
      error: (err) => console.error("Erreur de recherche:", err)
    });
  }

  /**
   * Déclenche la recherche en temps réel quand un filtre change
   */
  onFilterChange(): void {
    this.searchSubject.next();
  }

  /**
   * Navigation carrousel
   */
  prevImage(commerce: any, event: Event): void {
    event.stopPropagation();
    if (commerce.images && commerce.images.length > 1) {
      commerce.currentImg = (commerce.currentImg - 1 + commerce.images.length) % commerce.images.length;
    }
  }

  nextImage(commerce: any, event: Event): void {
    event.stopPropagation();
    if (commerce.images && commerce.images.length > 1) {
      commerce.currentImg = (commerce.currentImg + 1) % commerce.images.length;
    }
  }

  /**
   * Met à jour les points sur la carte
   */
  private actualiserCarte(commerces: any[]): void {
    this.pointsCarte = commerces.map(c => ({
      lat: c.localisations && c.localisations[0] ? c.localisations[0].geolocalisation?.y : null,
      lon: c.localisations && c.localisations[0] ? c.localisations[0].geolocalisation?.x : null,
      nom: c.nom,
      adresse: c.adresse || (c.localisations && c.localisations[0] ? c.localisations[0].adresse : '')
    })).filter(p => p.lat != null);

    // Ajouter la position utilisateur pour se repérer
    if ((this.filtres as any).lat && (this.filtres as any).lon) {
      this.pointsCarte.push({
        lat: (this.filtres as any).lat,
        lon: (this.filtres as any).lon,
        nom: "Ma position",
        isUser: true
      });
    }
  }
}
