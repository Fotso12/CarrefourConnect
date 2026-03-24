import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
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
  
  commerce = {
    nom: '',
    description: '',
    idcategorie: '',
    telephone1: '',
    email: '',
    siteweb: '',
    ville: 'Douala',
    quartier: '',
    adresse: '',
    lat: 4.0483,
    lon: 9.7144,
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
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
    private readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    this.chargerCategories();
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
   * Finalisation de l'ajout
   */
  enregistrer(): void {
    // Appel réel au service pour créer le commerce puis redirection
    // Build payload compatible with backend DTO
    const payload: any = {
      idcategorie: this.commerce.idcategorie,
      iduser: this.authService.getUser()?.id,
      nom: this.commerce.nom,
      description: this.commerce.description,
      telephone1: this.commerce.telephone1,
      email: this.commerce.email,
      siteweb: this.commerce.siteweb,
      localisations: [{ ville: this.commerce.ville, quartier: this.commerce.quartier, adresse: this.commerce.adresse, lat: this.commerce.lat, lon: this.commerce.lon }]
    };

    this.commerceService.create(payload).subscribe({
      next: (created) => {
        console.log('Commerce créé', created);
        this.router.navigate(['/commercant/commerces']);
      },
      error: (err) => {
        console.error('Erreur création commerce', err);
        // On reste sur la page et on peut afficher une erreur (à améliorer)
      }
    });
  }
}
