import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategorieService } from '../../../coeur/services/categorie.service';

@Component({
  selector: 'app-gestion-categories',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gestion-categories.component.html'
})
export class GestionCategoriesComponent implements OnInit {
  categories: any[] = [];
  loading = true;
  formCategorie = { nom: '', description: '', icone: '' };
  editionMode = false;
  categorieIdEnEdition: string | null = null;

  constructor(private categorieService: CategorieService) {}

  ngOnInit(): void {
    this.chargerCategories();
  }

  chargerCategories(): void {
    this.categorieService.getAll().subscribe({
      next: (data) => {
        this.categories = data;
        this.loading = false;
      },
      error: (err) => {
        console.error("Erreur chargement catégories:", err);
        this.loading = false;
      }
    });
  }

  ajouter(): void {
    if (!this.formCategorie.nom.trim()) return;
    this.categorieService.create(this.formCategorie).subscribe({
      next: (cat) => {
        this.categories.push(cat);
        this.formCategorie = { nom: '', description: '', icone: '' };
      },
      error: (err) => console.error("Erreur ajout:", err)
    });
  }

  modifier(cat: any): void {
    this.editionMode = true;
    this.categorieIdEnEdition = cat.idcategorie;
    this.formCategorie = { ...cat };
  }

  sauvegarderModification(): void {
    if (!this.formCategorie.nom.trim() || !this.categorieIdEnEdition) return;
    this.categorieService.update(this.categorieIdEnEdition, this.formCategorie).subscribe({
      next: (updated) => {
        const index = this.categories.findIndex(c => c.idcategorie === updated.idcategorie);
        if (index !== -1) this.categories[index] = updated;
        this.annulerEdition();
      },
      error: (err) => console.error("Erreur modification:", err)
    });
  }

  annulerEdition(): void {
    this.editionMode = false;
    this.categorieIdEnEdition = null;
    this.formCategorie = { nom: '', description: '', icone: '' };
  }

  supprimer(id: string): void {
    if (confirm('Supprimer cette catégorie ? Cela pourrait impacter les commerces associés.')) {
      this.categorieService.delete(id).subscribe({
        next: () => {
          this.categories = this.categories.filter(c => c.idcategorie !== id);
        },
        error: (err) => console.error("Erreur suppression:", err)
      });
    }
  }
}
