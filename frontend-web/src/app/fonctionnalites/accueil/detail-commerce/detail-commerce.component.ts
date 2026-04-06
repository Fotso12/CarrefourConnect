import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommerceService } from '../../../coeur/services/commerce.service';
import { AvisService } from '../../../coeur/services/avis.service';
import { OffreService } from '../../../coeur/services/offre.service';
import { AuthService } from '../../../coeur/services/auth.service';
import { CarteComponent } from '../../../partages/composants/carte/carte.component';
import { ModalComponent } from '../../../partages/composants/modal/modal.component';

@Component({
  selector: 'app-detail-commerce',
  standalone: true,
  imports: [CommonModule, RouterLink, CarteComponent, FormsModule, ModalComponent],
  templateUrl: './detail-commerce.component.html',
  styleUrl: './detail-commerce.component.css'
})
export class DetailCommerceComponent implements OnInit {
  commerce: any;
  loading = true;
  currentImg = 0;
  votrePosition: { lat: number, lon: number } | null = null;
  pointsCarte: any[] = [];
  routePoints: any[] = [];
  routeInfo: { distance: number, duration: number } | null = null;

  // Offres et Avis
  offres: any[] = [];
  avisList: any[] = [];
  showAvisModal = false;
  nouvelAvis = { note: 5, commentaire: '' };
  moyenneNote = 0;
  currentUser: any = null;

  constructor(
    private route: ActivatedRoute,
    private commerceService: CommerceService,
    private avisService: AvisService,
    private offreService: OffreService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUser();
    
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      // 1. Incrémenter les vues
      this.commerceService.incrementerViews(id).subscribe({
        error: (e) => console.error("Could not increment views:", e)
      });
      
      // 2. Récupérer le commerce
      this.commerceService.getById(id).subscribe(data => {
        this.commerce = data;
        this.loading = false;
        this.initialiserCarte();
      });

      // 3. Charger les offres
      this.offreService.getByCommerce(id).subscribe(data => {
        this.offres = data;
      });

      // 4. Charger les avis
      this.chargerAvis(id);
    }
  }

  chargerAvis(id: string): void {
    this.avisService.getByCommerce(id).subscribe(data => {
      this.avisList = data;
      this.calculerMoyenne();
    });
  }

  calculerMoyenne(): void {
    if (this.avisList.length === 0) {
      this.moyenneNote = 0;
      return;
    }
    const sum = this.avisList.reduce((acc, current) => acc + current.note, 0);
    this.moyenneNote = sum / this.avisList.length;
  }

  soumettreAvis(): void {
    if (!this.commerce?.idcommerce) return;
    if (!this.currentUser) {
      alert("Veuillez vous connecter pour laisser un avis.");
      return;
    }

    const payload = {
      idcommerce: this.commerce.idcommerce,
      iduser: this.currentUser.id,
      note: this.nouvelAvis.note,
      commentaire: this.nouvelAvis.commentaire
    };

    this.avisService.create(payload).subscribe({
      next: (res) => {
        this.showAvisModal = false;
        this.nouvelAvis = { note: 5, commentaire: '' };
        this.chargerAvis(this.commerce.idcommerce);
      },
      error: (e) => {
         console.error("Erreur avis", e);
         alert("Impossible de soumettre l'avis.");
      }
    });
  }

  nextImage(): void {
    if (this.commerce?.images?.length > 1) {
      this.currentImg = (this.currentImg + 1) % this.commerce.images.length;
    }
  }

  prevImage(): void {
    if (this.commerce?.images?.length > 1) {
      this.currentImg = (this.currentImg - 1 + this.commerce.images.length) % this.commerce.images.length;
    }
  }

  initialiserCarte(): void {
    if (!this.commerce?.localisations?.[0]) return;
    const loc = this.commerce.localisations[0];
    const lat = loc.geolocalisation?.y || loc.lat;
    const lon = loc.geolocalisation?.x || loc.lon;
    
    this.pointsCarte = [{
      lat: lat,
      lon: lon,
      nom: this.commerce.nom,
      adresse: loc.adresse,
      image: (this.commerce.images && this.commerce.images.length > 0) ? this.commerce.images[0].url : this.commerce.imagePrincipale,
      idcommerce: this.commerce.idcommerce
    }];
  }

  obtenirItineraire(): void {
    const loc = this.commerce?.localisations?.[0];
    const destLat = loc?.geolocalisation?.y || loc?.lat;
    const destLon = loc?.geolocalisation?.x || loc?.lon;

    if (!destLat || !destLon) {
      alert("Cet établissement n'a pas de coordonnées géographiques renseignées.");
      return;
    }

    if (!navigator.geolocation) {
      alert("La géolocalisation n'est pas supportée par votre navigateur.");
      return;
    }

    navigator.geolocation.getCurrentPosition((pos) => {
      const userLat = pos.coords.latitude;
      const userLon = pos.coords.longitude;
      
      this.votrePosition = { lat: userLat, lon: userLon };
      
      this.pointsCarte = [
        { 
          lat: destLat, 
          lon: destLon, 
          nom: this.commerce.nom, 
          adresse: loc.adresse,
          image: (this.commerce.images && this.commerce.images.length > 0) ? this.commerce.images[0].url : this.commerce.imagePrincipale,
          idcommerce: this.commerce.idcommerce 
        },
        { lat: userLat, lon: userLon, nom: "Ma Position", isUser: true }
      ];

      // Appel API OSRM pour obtenir le tracé routier réel
      const osrmUrl = `https://router.project-osrm.org/route/v1/driving/${userLon},${userLat};${destLon},${destLat}?overview=full&geometries=geojson`;
      
      fetch(osrmUrl)
        .then(res => res.json())
        .then(data => {
          if (data.routes && data.routes.length > 0) {
            // Convertir GeoJSON [lon, lat] en [lat, lon] pour Leaflet
            this.routePoints = data.routes[0].geometry.coordinates.map((c: any) => ({
              lat: c[1],
              lon: c[0]
            }));
            this.routeInfo = {
              distance: data.routes[0].distance, // en mètres
              duration: data.routes[0].duration  // en secondes
            };
          } else {
            // Fallback ligne droite
            this.routePoints = [
              { lat: userLat, lon: userLon },
              { lat: destLat, lon: destLon }
            ];
            this.routeInfo = null;
          }
        })
        .catch(err => {
          console.error("Erreur routage OSRM:", err);
          this.routePoints = [
            { lat: userLat, lon: userLon },
            { lat: destLat, lon: destLon }
          ];
        });
    }, (err) => {
      alert("Impossible de récupérer votre position : " + err.message);
    });
  }
}
