import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommerceService } from '../../../coeur/services/commerce.service';
import { CarteComponent } from '../../../partages/composants/carte/carte.component';

@Component({
  selector: 'app-detail-commerce',
  standalone: true,
  imports: [CommonModule, RouterLink, CarteComponent],
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

  constructor(
    private route: ActivatedRoute,
    private commerceService: CommerceService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.commerceService.getById(id).subscribe(data => {
        this.commerce = data;
        this.loading = false;
        this.initialiserCarte();
      });
    }
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
      adresse: loc.adresse
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
        { lat: destLat, lon: destLon, nom: this.commerce.nom, adresse: loc.adresse },
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
          } else {
            // Fallback ligne droite
            this.routePoints = [
              { lat: userLat, lon: userLon },
              { lat: destLat, lon: destLon }
            ];
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
