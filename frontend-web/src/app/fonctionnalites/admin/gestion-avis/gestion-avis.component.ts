import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AvisService } from '../../../coeur/services/avis.service';

@Component({
  selector: 'app-gestion-avis-admin',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="space-y-12">
      <div class="flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
        <div>
          <h1 class="text-4xl font-bold text-slate-800 tracking-tight">Avis Clients</h1>
          <p class="text-slate-400 font-bold mt-2 uppercase text-[10px] tracking-[0.2em]">Consultation et modération des retours</p>
        </div>
        <div class="bg-[#034d92]/5 text-[#034d92] px-6 py-3 rounded-2xl text-[10px] font-bold uppercase tracking-widest border border-[#034d92]/10">
            {{ avisList.length }} avis au total
        </div>
      </div>

      <div *ngIf="loading" class="grid grid-cols-1 gap-6">
          <div *ngFor="let i of [1,2,3]" class="h-32 bg-white rounded-[40px] animate-pulse border border-slate-50"></div>
      </div>

      <div *ngIf="!loading && avisList.length === 0" class="bg-white p-24 rounded-[40px] text-center border border-slate-100 shadow-sm flex flex-col items-center">
          <p class="text-slate-400 font-bold uppercase text-xs tracking-widest">Aucun avis enregistré pour le moment.</p>
      </div>

      <div class="grid grid-cols-1 gap-8">
        <div *ngFor="let avis of pagedAvis" 
             class="bg-white p-8 rounded-[40px] shadow-sm border border-slate-50 group hover:shadow-2xl transition-all duration-500">
          <div class="flex flex-col lg:flex-row gap-8">
            <!-- User Info & Rating -->
            <div class="lg:w-1/4">
                <div class="flex items-center gap-4 mb-4">
                    <div class="h-12 w-12 rounded-2xl bg-slate-50 flex items-center justify-center text-[#034d92] font-black text-lg">
                        {{ avis.iduser?.charAt(0) || 'V' }}
                    </div>
                    <div>
                        <p class="font-black text-slate-800 text-sm">Visiteur</p>
                        <p class="text-[10px] text-slate-400">{{ avis.datePublication | date:'dd/MM/yyyy HH:mm' }}</p>
                    </div>
                </div>
                <div class="flex items-center gap-1">
                    <i *ngFor="let star of [1,2,3,4,5]" 
                       class="fa-solid fa-star text-xs" 
                       [class.text-orange-400]="star <= avis.note"
                       [class.text-slate-200]="star > avis.note"></i>
                    <span class="ml-2 font-black text-slate-800">{{ avis.note }}</span>
                </div>
            </div>

            <!-- Content -->
            <div class="flex-1">
                <div class="flex items-center gap-2 mb-4">
                    <p class="text-[10px] font-bold text-[#034d92] uppercase tracking-widest bg-blue-50 px-3 py-1 rounded-lg">
                        <i class="fa-solid fa-store mr-2 opacity-50"></i>{{ avis.nomCommerce || 'Établissement inconnu' }}
                    </p>
                    <span [class]="'px-3 py-1 rounded-full text-[9px] font-bold uppercase tracking-widest ' + getStatusClass(avis.status)">
                        {{ avis.status }}
                    </span>
                </div>
                <p class="text-slate-600 leading-relaxed italic text-sm">"{{ avis.commentaire }}"</p>
                
                <!-- Response if exists -->
                <div *ngIf="avis.reponse" class="mt-6 p-6 bg-slate-50 rounded-[32px] border border-slate-100 relative group-hover:bg-white transition-colors">
                    <div class="flex items-center gap-2 mb-2">
                        <i class="fa-solid fa-reply text-[10px] text-orange-500"></i>
                        <span class="text-[9px] font-black text-orange-500 uppercase tracking-widest">Réponse du Commerçant</span>
                    </div>
                    <p class="text-slate-500 text-xs italic">{{ avis.reponse }}</p>
                    <p class="text-[9px] font-bold text-slate-300 mt-2">{{ avis.dateReponse | date:'dd/MM/yyyy HH:mm' }}</p>
                </div>
            </div>
          </div>
        </div>

        <!-- Pagination Controls -->
        <div *ngIf="totalPages > 1" class="flex items-center justify-center gap-4 mt-8 bg-white/50 backdrop-blur-sm p-4 rounded-[32px] border border-slate-100 w-fit mx-auto">
            <button (click)="prevPage()" [disabled]="currentPage === 1" 
                    class="w-12 h-12 rounded-2xl flex items-center justify-center transition-all"
                    [class]="currentPage === 1 ? 'text-slate-200 cursor-not-allowed' : 'text-slate-600 hover:bg-white hover:shadow-lg hover:text-[#034d92]'">
                <i class="fa-solid fa-chevron-left"></i>
            </button>
            
            <div class="flex items-center px-6">
                <span class="text-[10px] font-black uppercase tracking-[0.2em] text-slate-400">Page</span>
                <span class="mx-3 text-sm font-black text-slate-800">{{ currentPage }} / {{ totalPages }}</span>
            </div>

            <button (click)="nextPage()" [disabled]="currentPage === totalPages" 
                    class="w-12 h-12 rounded-2xl flex items-center justify-center transition-all"
                    [class]="currentPage === totalPages ? 'text-slate-200 cursor-not-allowed' : 'text-slate-600 hover:bg-white hover:shadow-lg hover:text-[#034d92]'">
                <i class="fa-solid fa-chevron-right"></i>
            </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host { display: block; }
  `]
})
export class GestionAvisAdminComponent implements OnInit {
  avisList: any[] = [];
  loading = true;

  // Pagination
  currentPage = 1;
  pageSize = 15;

  constructor(private avisService: AvisService) {}

  ngOnInit(): void {
    this.chargerAvis();
  }

  chargerAvis(): void {
    this.loading = true;
    this.avisService.getAll().subscribe({
      next: (data) => {
        this.avisList = data.sort((a, b) => new Date(b.datePublication).getTime() - new Date(a.datePublication).getTime());
        this.loading = false;
        this.currentPage = 1; // Reset to page 1
      },
      error: (err) => {
        console.error("Erreur chargement avis:", err);
        this.loading = false;
      }
    });
  }

  get pagedAvis(): any[] {
    const startIndex = (this.currentPage - 1) * this.pageSize;
    return this.avisList.slice(startIndex, startIndex + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.avisList.length / this.pageSize);
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) this.currentPage++;
  }

  prevPage(): void {
    if (this.currentPage > 1) this.currentPage--;
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'APPROUVE': return 'bg-emerald-50 text-emerald-500 border border-emerald-100';
      case 'REJETE': return 'bg-red-50 text-red-500 border border-red-100';
      default: return 'bg-amber-50 text-amber-500 border border-amber-100';
    }
  }

  supprimer(id: string): void {
    if (confirm('Voulez-vous vraiment supprimer cet avis ?')) {
      this.avisService.delete(id).subscribe(() => this.chargerAvis());
    }
  }
}
