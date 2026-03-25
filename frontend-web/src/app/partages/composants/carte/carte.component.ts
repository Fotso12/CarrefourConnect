import { Component, Input, Output, EventEmitter, OnInit, AfterViewInit, OnChanges, SimpleChanges, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import * as L from 'leaflet';

/**
 * Composant de carte interactive utilisant Leaflet
 * Affiche des marqueurs pour chaque commerce
 */
@Component({
  selector: 'app-carte',
  standalone: true,
  imports: [CommonModule],
  template: '<div #mapContainer class="h-full w-full rounded-2xl"></div>',
  styles: [`
    :host { display: block; }
    .map-inner { min-height: 240px; z-index: 0; }
  `]
})
export class CarteComponent implements OnInit, AfterViewInit, OnChanges {
  @Input() points: any[] = []; // Liste des coordonnées et infos à afficher
  @Input() routePoints: any[] = []; // Points pour tracer une ligne (ex: itinéraire)
  @Input() minHeight: number = 240;
  @Output() mapClick: EventEmitter<any> = new EventEmitter<any>();
  @ViewChild('mapContainer', { static: true }) mapContainer!: ElementRef<HTMLDivElement>;
  private map: L.Map | undefined;

  constructor() { }

  ngOnInit(): void {
    // Lifecycle hook present to satisfy Angular's OnInit interface.
    // Initialization that depends on the DOM occurs in ngAfterViewInit().
  }

  ngAfterViewInit(): void {
    this.initMap(); // Initialisation de Leaflet après le rendu de la vue
  }

  ngOnChanges(changes: SimpleChanges): void {
    // Si les points ou l'itinéraire changent, on met à jour les marqueurs sur la carte
    if ((changes['points'] || changes['routePoints']) && this.map) {
      this.updateMarkers();
    }
  }

  /**
   * Initialise la carte centrée sur Douala
   */
  private initMap(): void {
    // Create the map inside the container element (avoid fixed IDs)
    // Correction des icônes par défaut de Leaflet (utilisation des assets locaux)
    const iconRetinaUrl = 'assets/leaflet/marker-icon-2x.png';
    const iconUrl = 'assets/leaflet/marker-icon.png';
    const shadowUrl = 'assets/leaflet/marker-shadow.png';
    const iconDefault = L.icon({
      iconRetinaUrl,
      iconUrl,
      shadowUrl,
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      shadowSize: [41, 41]
    });
    L.Marker.prototype.options.icon = iconDefault;
    
    // Fallback pour les icônes par défaut
    (L.Icon.Default.prototype as any)._getIconUrl = (name: string) => `assets/leaflet/marker-${name}.png`;

    this.map = L.map(this.mapContainer.nativeElement, {
      center: [4.0511, 9.7679],
      zoom: 13
    });

    // Ensure container has a reasonable min height
    this.mapContainer.nativeElement.style.minHeight = this.minHeight + 'px';

    // Couche OpenStreetMap
    if (!this.map) return;
    const tileLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 18,
      minZoom: 3,
      attribution: '&copy; OpenStreetMap'
    });
    tileLayer.addTo(this.map);

    // Emit clicks on the map so parent can update coordinates
    this.map.on('click', (e: any) => {
      const lat = e.latlng?.lat;
      const lng = e.latlng?.lng;
      this.mapClick.emit({ lat, lng });
    });
  }

  /**
   * Ajoute les marqueurs sur la carte dynamiquement
   */
  private updateMarkers(): void {
    if (!this.map) return;

    // Remove existing markers and polylines
    (this.map as any).eachLayer((layer: any) => {
      if (layer instanceof L.Marker || layer instanceof L.Polyline) {
        this.map?.removeLayer(layer);
      }
    });

    // Dessiner l'itinéraire si présent
    if (this.routePoints && this.routePoints.length >= 2) {
      const path = this.routePoints.map(p => [p.lat, p.lon] as L.LatLngExpression);
      // Tracé principal (Orange thématique)
      L.polyline(path, { 
        color: '#F78F1E', 
        weight: 6, 
        opacity: 0.8, 
        lineJoin: 'round' 
      }).addTo(this.map);
      
      this.map.fitBounds(L.polyline(path).getBounds(), { padding: [50, 50] });
    }

    this.points.forEach(point => {
      const lat = point.lat ?? point.latitude ?? point.latitud;
      const lon = point.lon ?? point.longitude ?? point.lng;
      if (lat != null && lon != null && this.map) {
        const marker = L.marker([lat, lon])
          .addTo(this.map)
          .bindPopup(`<b>${point.nom || ''}</b><br>${point.adresse || ''}`);
          
        if (point.isUser) {
          marker.setIcon(L.divIcon({
            className: 'user-marker',
            html: '<div class="w-4 h-4 bg-[#00ADEF] border-2 border-white rounded-full shadow-lg pulse"></div>',
            iconSize: [16, 16],
            iconAnchor: [8, 8]
          }));
        }
      }
    });
  }
}
