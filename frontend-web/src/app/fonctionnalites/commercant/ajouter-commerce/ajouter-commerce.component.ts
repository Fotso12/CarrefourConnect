import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CommerceService } from '../../../coeur/services/commerce.service';
import { CategorieService } from '../../../coeur/services/categorie.service';
import { AuthService } from '../../../coeur/services/auth.service';
import { CarteComponent } from '../../../partages/composants/carte/carte.component';

/**
 * Composant Wizard pour l'ajout d'un commerce
 */
@Component({
  selector: 'app-ajouter-commerce',
  standalone: true,
  imports: [CommonModule, FormsModule, CarteComponent],
  templateUrl: './ajouter-commerce.component.html',
  styleUrl: './ajouter-commerce.component.css'
})
export class AjouterCommerceComponent implements OnInit {
  step = 1;
  categories: any[] = [];
  isEdit = false;
  showSuccessModal = false;
  
  commerce = {
    idcommerce: '',
    nom: '',
    description: '',
    idcategorie: '',
    telephone1: '',
    telephone2: '',
    email: '',
    siteweb: '',
    heureOuverture: '',
    heureFermeture: '',
    ville: 'Douala',
    quartier: '',
    adresse: '',
    lat: 4.0483,
    lon: 9.7144,
    statut: '',
    images: [] as any[]
  };

  nouvelleCategorie = {
    nom: '',
    description: '',
    icone: 'store'
  };
  showAddCategory = false;
  // File upload
  selectedFiles: File[] = [];

  constructor(
    private readonly commerceService: CommerceService,
    private readonly categorieService: CategorieService,
    public readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly cdr: ChangeDetectorRef,
    private readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    this.chargerCategories();
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.commerce.idcommerce = id; // Sécuriser l'ID immédiatement
      this.commerceService.getById(id).subscribe({
        next: (data) => {
          Object.assign(this.commerce, data);
          if (data.idcategorie) this.commerce.idcategorie = data.idcategorie;
          if (data.localisations && data.localisations.length > 0) {
            const loc = data.localisations[0];
            this.commerce.ville = loc.ville;
            this.commerce.quartier = loc.quartier;
            this.commerce.adresse = loc.adresse;
            this.commerce.lat = loc.lat;
            this.commerce.lon = loc.lon;
          }
        },
        error: (err) => {
          console.error("Erreur chargement commerce, utilisation de l'ID de l'URL", err);
        }
      });
    }
  }

  chargerCategories(): void {
    this.categorieService.getAll().subscribe(data => this.categories = data);
  }

  nextStep(): void {
    if (this.step < 4) this.step++;
  }

  prevStep(): void {
    if (this.step > 1) this.step--;
  }

  /**
   * Ajoute une nouvelle catégorie à la volée
   */
  creerCategorie(): void {
    if (!this.nouvelleCategorie.nom) return;
    
    // Appel au service (à implémenter dans CategorieService)
    this.categorieService.create(this.nouvelleCategorie).subscribe(newCat => {
      this.categories.push(newCat);
      this.commerce.idcategorie = newCat.id;
      this.showAddCategory = false;
      this.nouvelleCategorie = { nom: '', description: '', icone: 'store' };
    });
  }

  /**
   * Met à jour la position depuis la carte
   */
  onMapClick(event: any): void {
    this.commerce.lat = event.lat;
    this.commerce.lon = event.lng;
  }

  /** File handling */
  onFilesSelected(event: any): void {
    const files: FileList = event.target.files;
    for (let i = 0; i < files.length; i++) {
      this.selectedFiles.push(files.item(i) as File);
    }
    // Optionally add to commerce.images for preview/upload
    this.commerce.images = this.selectedFiles.map(f => ({ name: f.name, file: f, url: URL.createObjectURL(f) }));
    this.cdr.detectChanges();
  }

  removeImage(index: number): void {
    const img = this.commerce.images[index];
    if (img?.url) {
      try { URL.revokeObjectURL(img.url); } catch {}
    }
    this.selectedFiles.splice(index, 1);
    this.commerce.images.splice(index, 1);
  }

  useGeolocation(): void {
    if (!navigator.geolocation) return;
    navigator.geolocation.getCurrentPosition((pos) => {
      this.commerce.lat = pos.coords.latitude;
      this.commerce.lon = pos.coords.longitude;
      this.cdr.detectChanges();
    }, (err) => {
      console.error('Geolocation error', err);
    }, { enableHighAccuracy: true, timeout: 10000 });
  }

  /**
   * Cherche les coordonnées GPS à partir de la ville et du quartier
   */
  localiserQuartier(): void {
    const query = `${this.commerce.quartier}, ${this.commerce.ville}, Cameroun`;
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&limit=1`;

    fetch(url)
      .then(res => res.json())
      .then(results => {
        if (results && results.length > 0) {
          this.commerce.lat = parseFloat(results[0].lat);
          this.commerce.lon = parseFloat(results[0].lon);
          this.cdr.detectChanges();
        } else {
          alert("Lieu introuvable. Veuillez placer le marqueur manuellement.");
        }
      })
      .catch(err => {
        console.error("Erreur Nominatim:", err);
        alert("Erreur lors de la recherche du quartier.");
      });
  }

  /**
   * Finalisation de l'ajout ou modification
   */
  enregistrer(): void {
    const payload: any = {
      idcommerce: this.commerce.idcommerce || undefined,
      idcategorie: this.commerce.idcategorie,
      iduser: this.authService.getUser()?.id,
      nom: this.commerce.nom,
      description: this.commerce.description,
      telephone1: this.commerce.telephone1,
      telephone2: this.commerce.telephone2,
      email: this.commerce.email,
      siteweb: this.commerce.siteweb,
      statut: this.commerce.statut || undefined,
      heureOuverture: this.commerce.heureOuverture,
      heureFermeture: this.commerce.heureFermeture,
      localisations: [{ 
        ville: this.commerce.ville, 
        quartier: this.commerce.quartier, 
        adresse: this.commerce.adresse, 
        lat: this.commerce.lat, 
        lon: this.commerce.lon 
      }]
    };

    const action = this.isEdit 
      ? this.commerceService.update(this.commerce.idcommerce, payload)
      : this.commerceService.create(payload);

    action.subscribe({
      next: (result) => {
        const commerceId = result.idcommerce || result.id;
        
        // Upload des images s'il y en a
        if (this.selectedFiles.length > 0 && commerceId) {
          this.selectedFiles.forEach((file, index) => {
            this.commerceService.uploadMedia(file, commerceId, index === 0).subscribe();
          });
        }
        
        this.showSuccessModal = true;
      },
      error: (err) => console.error('Erreur lors de l\'enregistrement', err)
    });
  }

  fermerModal(): void {
    this.showSuccessModal = false;
    this.router.navigate(['/commercant/commerces']);
  }
}
