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
    // Si les points changent, on met à jour les marqueurs sur la carte
    if (changes['points'] && this.map) {
      this.updateMarkers();
    }
  }

  /**
   * Initialise la carte centrée sur Douala
   */
  private initMap(): void {
    // Create the map inside the container element (avoid fixed IDs)
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

    // Remove existing markers
    (this.map as any).eachLayer((layer: any) => {
      if (layer instanceof L.Marker) {
        this.map?.removeLayer(layer);
      }
    });

    this.points.forEach(point => {
      const lat = point.lat ?? point.latitude ?? point.latitud;
      const lon = point.lon ?? point.longitude ?? point.lng;
      if (lat != null && lon != null && this.map) {
        L.marker([lat, lon])
          .addTo(this.map)
          .bindPopup(`<b>${point.nom || ''}</b><br>${point.adresse || ''}`);
      }
    });
  }
}
