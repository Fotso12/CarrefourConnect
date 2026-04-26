import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CommerceService } from '../../../coeur/services/commerce.service';
import { CategorieService } from '../../../coeur/services/categorie.service';
import { AuthService } from '../../../coeur/services/auth.service';
import { AbonnementService } from '../../../coeur/services/abonnement.service';
import { CarteComponent } from '../../../partages/composants/carte/carte.component';
import { ModalComponent } from '../../../partages/composants/modal/modal.component';
import { HttpClient } from '@angular/common/http';
import { OffreService } from '../../../coeur/services/offre.service';

/**
 * Composant Wizard pour l'ajout d'un commerce
 */
@Component({
  selector: 'app-ajouter-commerce',
  standalone: true,
  imports: [CommonModule, FormsModule, CarteComponent, ModalComponent],
  templateUrl: './ajouter-commerce.component.html',
  styleUrl: './ajouter-commerce.component.css'
})
export class AjouterCommerceComponent implements OnInit {
  step = 1;
  categories: any[] = [];
  abonnements: any[] = [];
  selectedAbonnement: any = null;
  
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
    idabonnement: '',
    images: [] as any[]
  };

  // Offre Spéciale
  hasSpecialOffer = false;
  offreSpeciale = {
    titre: '',
    description: '',
    type: 'PROMOTION',
    reduction: 0,
    dateDebut: '',
    dateFin: ''
  };

  // Paiement
  showPaymentModal = false;
  paymentMode: 'OM' | 'MOMO' | '' = '';
  paymentPhone = '';
  paymentAmountStr = '';
  paymentError = '';
  amountToPay = 0; // Store exact price difference

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
    private readonly abonnementService: AbonnementService,
    private readonly http: HttpClient,
    public readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly cdr: ChangeDetectorRef,
    private readonly authService: AuthService,
    private readonly offreService: OffreService
  ) {}

  ngOnInit(): void {
    this.chargerCategories();
    this.chargerAbonnements();
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

  chargerAbonnements(): void {
    this.abonnementService.getAll().subscribe({
      next: (data) => {
        // Dédupliquer par type/nom et mapper les prix
        const seenTypes = new Set<string>();
        this.abonnements = data
          .filter(ab => {
            const key = (ab.type || ab.nom || '').toLowerCase();
            if (seenTypes.has(key)) return false;
            seenTypes.add(key);
            return true;
          })
          .map(ab => {
            let prix = 0;
            const nomOuType = (ab.type || ab.nom || '').toLowerCase();
            if (nomOuType.includes('basique')) prix = 5000;
            else if (nomOuType.includes('premium')) prix = 10000;
            else if (nomOuType.includes('gold')) prix = 15000;
            else prix = 5000; // default
            return { ...ab, nomAffiche: ab.type || ab.nom, prixAffiche: prix };
          });
      },
      error: (err) => console.error("Erreur abonnements", err)
    });
  }

  nextStep(): void {
    if (this.step < 5) this.step++;
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
      this.commerce.idcategorie = newCat.idcategorie;
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
   * Cherche les coordonnées GPS à partir de la ville et du quartier avec timeout et retry
   */
  localiserQuartier(retry = 0): void {
    if (!this.commerce.quartier || !this.commerce.ville) {
      alert("Veuillez entrer la ville et le quartier.");
      return;
    }

    const query = `${this.commerce.quartier}, ${this.commerce.ville}, Cameroon`;
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&limit=1&timeout=10`;

    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 8000); // 8 secondes timeout

    fetch(url, { signal: controller.signal })
      .then(res => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json();
      })
      .then(results => {
        clearTimeout(timeoutId);
        if (results && results.length > 0) {
          this.commerce.lat = parseFloat(results[0].lat);
          this.commerce.lon = parseFloat(results[0].lon);
          console.log(`✓ Localisation trouvée: ${this.commerce.lat}, ${this.commerce.lon}`);
          this.cdr.detectChanges();
        } else {
          alert("Lieu introuvable. Veuillez placer le marqueur manuellement sur la carte.");
        }
      })
      .catch(err => {
        clearTimeout(timeoutId);
        console.error("Erreur Nominatim:", err);
        
        // Retry logic: retry once if network error or timeout
        if (retry < 1 && (err.name === 'AbortError' || err.message.includes('fetch'))) {
          console.warn("Tentative de localisation échouée. Nouvelle tentative...");
          setTimeout(() => this.localiserQuartier(retry + 1), 2000);
        } else {
          alert("Erreur lors de la recherche du quartier. Veuillez réessayer ou placer le marqueur manuellement.");
        }
      });
  }

  /**
   * Déclenche la modale de paiement si tout est ok
   */
  preparerPaiement(): void {
    if (!this.selectedAbonnement) {
      alert("Veuillez choisir un abonnement avant de finaliser l'inscription.");
      return;
    }
    
    let requiredPrice = this.selectedAbonnement.prixAffiche;

    if (this.isEdit && this.commerce.idabonnement) {
      // Find old abonnement
      const oldAb = this.abonnements.find(a => a.idabonnement === this.commerce.idabonnement);
      if (oldAb) {
        if (requiredPrice > oldAb.prixAffiche) {
          requiredPrice = requiredPrice - oldAb.prixAffiche; // Pay only the difference
        } else if (requiredPrice <= oldAb.prixAffiche) {
           this.commerce.idabonnement = this.selectedAbonnement.idabonnement;
           this.enregistrer();
           return;
        }
      }
    }
    
    this.amountToPay = requiredPrice;
    this.commerce.idabonnement = this.selectedAbonnement.idabonnement;
    this.paymentMode = '';
    this.paymentPhone = '';
    this.paymentAmountStr = requiredPrice.toString();
    this.paymentError = '';
    this.showPaymentModal = true;
  }

  /**
   * Validation stricte du paiement selon les règles
   */
  validerPaiement(): void {
    this.paymentError = '';
    
    // 1. Validation du téléphone (exactement 9 chiffres, commence par 6)
    if (!/^6\d{8}$/.test(this.paymentPhone)) {
      this.paymentError = "Le numéro de téléphone doit contenir exactement 9 chiffres et commencer par '6'.";
      return;
    }

    // 2. Validation du montant (strictement des chiffres, pas de . ou , et pas négatif)
    if (!/^\d+$/.test(this.paymentAmountStr)) {
      this.paymentError = "Le montant doit être un nombre entier valide (sans lettres, ni virgule, ni point).";
      return;
    }

    const amount = parseInt(this.paymentAmountStr, 10);
    const requiredPrice = this.amountToPay;

    if (amount < requiredPrice) {
      this.paymentError = `Erreur : Le montant de ${amount} FCFA est insuffisant. Vous devez payer ${requiredPrice} FCFA.`;
      return;
    }

    if (amount > requiredPrice) {
      this.paymentError = `Erreur : Le montant est supérieur au prix attendu. Veuillez saisir exactement ${requiredPrice} FCFA.`;
      return;
    }

    // Si tout est ok (amount === requiredPrice)
    this.showPaymentModal = false;
    this.enregistrer();
  }

  /**
   * Finalisation de l'ajout ou modification après paiement
   */
  enregistrer(): void {
    const payload: any = {
      idcommerce: this.commerce.idcommerce || undefined,
      idcategorie: this.commerce.idcategorie,
      idabonnement: this.commerce.idabonnement || undefined,
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
        
        // Traitement de l'Offre Spéciale
        if (this.hasSpecialOffer && commerceId) {
          const offrePayload = {
            idcommerce: commerceId,
            titre: this.offreSpeciale.titre,
            description: this.offreSpeciale.description,
            type: this.offreSpeciale.type,
            reduction: this.offreSpeciale.reduction,
            dateDebut: this.offreSpeciale.dateDebut,
            dateFin: this.offreSpeciale.dateFin
          };
          this.offreService.create(offrePayload).subscribe({
            next: () => console.log("Offre spéciale créée"),
            error: (e) => console.error("Erreur création offre:", e)
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
