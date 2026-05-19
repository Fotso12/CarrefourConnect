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
        // Tri discret des commerces selon leur niveau d'abonnement (Gold > Premium > Basique > Free)
        const sortedData = [...data].sort((a: any, b: any) => {
          const getWeight = (c: any) => {
            if (!c.abonnement) return 0;
            const type = c.abonnement.type?.toUpperCase();
            if (type === 'GOLD') return 3;
            if (type === 'PREMIUM') return 2;
            if (type === 'BASIQUE') return 1;
            return 0;
          };
          return getWeight(b) - getWeight(a);
        });

        // Initialise l'index de l'image courante pour le carrousel
        this.commerces = sortedData.map(c => ({ ...c, currentImg: 0 }));
        this.actualiserCarte(sortedData);
      },
      error: (err) => console.error("Erreur de recherche:", err)
    });
  }

  /**
   * Nettoie et valide un numéro de téléphone camerounais (9 chiffres, commençant par 6)
   */
  getTelephoneFormatte(commerce: any): string | null {
    if (!commerce || !commerce.telephone) return null;
    let tel = commerce.telephone.toString().replace(/\D/g, '');
    
    if (tel.length > 9 && tel.startsWith('237')) {
      tel = tel.substring(tel.length - 9);
    }
    
    if (tel.length === 9 && tel.startsWith('6')) {
      return tel;
    }
    
    if (tel.length < 9) {
      if (!tel.startsWith('6')) {
        tel = '6' + tel;
      }
      while (tel.length < 9) {
        tel += '0';
      }
      return tel.substring(0, 9);
    }
    
    if (tel.length === 9 && !tel.startsWith('6')) {
      tel = '6' + tel.substring(1);
      return tel;
    }
    
    return tel.substring(0, 9);
  }

  getTelephoneUrl(commerce: any): string {
    const tel = this.getTelephoneFormatte(commerce);
    return tel ? `tel:+237${tel}` : '';
  }

  getWhatsAppUrl(commerce: any): string {
    const tel = this.getTelephoneFormatte(commerce);
    if (!tel) return '';
    return `https://wa.me/237${tel}`;
  }

  isEligibleWhatsApp(commerce: any): boolean {
    if (!commerce || !commerce.abonnement) return false;
    const type = commerce.abonnement.type?.toUpperCase();
    return type === 'GOLD' || type === 'PREMIUM';
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
    this.pointsCarte = commerces.map(c => {
      const loc = c.localisations && c.localisations.length > 0 ? c.localisations[0] : null;
      return {
        lat: loc ? (loc.geolocalisation?.y || loc.lat || loc.latitude) : null,
        lon: loc ? (loc.geolocalisation?.x || loc.lon || loc.longitude) : null,
        nom: c.nom,
        adresse: c.adresse || (loc ? loc.adresse : ''),
        image: (c.images && c.images.length > 0) ? c.images[0].url : c.imagePrincipale,
        idcommerce: c.idcommerce
      };
    }).filter(p => p.lat != null);

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
