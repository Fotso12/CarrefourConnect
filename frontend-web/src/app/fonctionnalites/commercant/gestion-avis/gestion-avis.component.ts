import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AvisService } from '../../../coeur/services/avis.service';
import { CommerceService } from '../../../coeur/services/commerce.service';
import { AuthService } from '../../../coeur/services/auth.service';
import { forkJoin, map, of, switchMap } from 'rxjs';

@Component({
  selector: 'app-gestion-avis-commercant',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="space-y-12">
      <div class="flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
        <div>
          <h1 class="text-4xl font-black text-slate-800 tracking-tight">Avis Clients</h1>
          <p class="text-slate-400 font-bold mt-2 uppercase text-[10px] tracking-[0.2em]">Retours d'expérience sur vos établissements</p>
        </div>
        <div class="bg-[#f97316]/5 text-[#f97316] px-6 py-3 rounded-2xl text-[10px] font-black uppercase tracking-widest border border-[#f97316]/10">
            {{ totalAvis }} avis reçu(s)
        </div>
      </div>

      <div *ngIf="loading" class="grid grid-cols-1 gap-6">
          <div *ngFor="let i of [1,2,3]" class="h-48 bg-white rounded-[40px] animate-pulse border border-slate-50"></div>
      </div>

      <div *ngIf="!loading && avisList.length === 0" class="bg-white p-24 rounded-[40px] text-center border border-slate-100 shadow-sm flex flex-col items-center">
          <div class="h-20 w-20 bg-slate-50 rounded-[32px] flex items-center justify-center text-slate-200 mb-6">
              <i class="fa-solid fa-comments text-4xl"></i>
          </div>
          <p class="text-slate-400 font-bold uppercase text-xs tracking-widest leading-relaxed max-w-xs text-center">Vous n'avez pas encore reçu d'avis pour vos établissements.</p>
      </div>

      <div class="grid grid-cols-1 gap-8">
        <div *ngFor="let cAvis of avisList" class="space-y-6">
            <h2 class="text-xs font-black text-slate-400 uppercase tracking-[0.3em] pl-4">{{ cAvis.commerceNom }}</h2>
            
            <div *ngFor="let avis of cAvis.avis" 
                 class="bg-white p-10 rounded-[40px] shadow-sm border border-slate-50 group hover:shadow-2xl transition-all duration-500">
                <div class="flex flex-col lg:flex-row gap-10">
                    <!-- Visiteur Info -->
                    <div class="lg:w-1/4">
                        <div class="flex items-center gap-4 mb-4">
                            <div class="h-14 w-14 rounded-[22px] bg-slate-50 flex items-center justify-center text-[#f97316] font-black text-xl">
                                {{ avis.iduser?.charAt(0) || 'V' }}
                            </div>
                            <div>
                                <p class="font-black text-slate-800 text-sm">Visiteur Anonyme</p>
                                <p class="text-[10px] text-slate-400 font-bold uppercase tracking-widest">{{ avis.datePublication | date:'dd MMM yyyy' }}</p>
                            </div>
                        </div>
                        <div class="flex items-center gap-1">
                            <i *ngFor="let star of [1,2,3,4,5]" 
                               class="fa-solid fa-star text-xs" 
                               [class.text-orange-400]="star <= avis.note"
                               [class.text-slate-100]="star > avis.note"></i>
                            <span class="ml-3 font-black text-slate-800">{{ avis.note }}/5</span>
                        </div>
                    </div>

                    <!-- Content & Reply -->
                    <div class="flex-1">
                        <p class="text-slate-600 leading-relaxed italic text-base mb-8">"{{ avis.commentaire }}"</p>
                        
                        <!-- Reply Section -->
                        <div *ngIf="!avis.reponse && draftingId !== avis.idavis" class="pt-4 border-t border-slate-50">
                            <button (click)="startDraft(avis.idavis)" class="text-[#f97316] text-[10px] font-black uppercase tracking-widest flex items-center gap-2 hover:gap-4 transition-all">
                                <i class="fa-solid fa-reply"></i> Répondre à cet avis
                            </button>
                        </div>

                        <!-- Draft Input -->
                        <div *ngIf="draftingId === avis.idavis" class="mt-6 space-y-4">
                            <textarea [(ngModel)]="replyText" 
                                      placeholder="Saisissez votre réponse..." 
                                      class="w-full bg-slate-50 border-none rounded-[24px] p-6 text-sm text-slate-600 focus:ring-2 focus:ring-[#f97316]/20 transition-all min-h-[120px]"></textarea>
                            <div class="flex gap-2">
                                <button (click)="draftingId = null" class="px-6 py-3 rounded-2xl text-[10px] font-black uppercase tracking-widest text-slate-400 hover:bg-slate-50 transition-all">Annuler</button>
                                <button (click)="envoyerReponse(avis)" class="px-8 py-3 bg-[#f97316] text-white rounded-2xl text-[10px] font-black uppercase tracking-widest shadow-lg shadow-orange-500/20 hover:scale-105 transition-all">Publier la réponse</button>
                            </div>
                        </div>

                        <!-- Published Response -->
                        <div *ngIf="avis.reponse && draftingId !== avis.idavis" class="mt-8 p-8 bg-[#f97316]/5 rounded-[32px] border border-[#f97316]/10 relative group-hover:bg-white transition-colors">
                            <div class="flex items-center justify-between mb-4">
                                <div class="flex items-center gap-3">
                                    <div class="h-8 w-8 rounded-xl bg-[#f97316] flex items-center justify-center text-white text-xs">
                                        <i class="fa-solid fa-reply"></i>
                                    </div>
                                    <span class="text-[10px] font-black text-[#f97316] uppercase tracking-widest">Votre Réponse</span>
                                </div>
                                <span class="text-[9px] font-bold text-slate-300">{{ avis.dateReponse | date:'dd/MM/yyyy' }}</span>
                            </div>
                            <p class="text-slate-600 text-sm italic leading-relaxed">{{ avis.reponse }}</p>
                            <button (click)="startDraft(avis.idavis, avis.reponse)" class="mt-4 text-[9px] font-bold text-slate-400 hover:text-[#f97316] transition-colors uppercase tracking-widest">Modifier la réponse</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host { display: block; }
  `]
})
export class GestionAvisCommercantComponent implements OnInit {
  avisList: any[] = [];
  loading = true;
  totalAvis = 0;
  draftingId: string | null = null;
  replyText = '';

  constructor(
    private avisService: AvisService,
    private commerceService: CommerceService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.chargerDonnees();
  }

  chargerDonnees(): void {
    this.loading = true;
    this.totalAvis = 0;
    
    this.authService.currentUser.pipe(
      switchMap(user => {
        const userId = user?.iduser || user?.id;
        if (!userId) return of([]);
        return this.commerceService.getByCommercant(userId);
      }),
      switchMap(commerces => {
        if (!commerces || commerces.length === 0) return of([]);
        
        const obsList = commerces.map((c: any) => 
          this.avisService.getByCommerce(c.idcommerce).pipe(
            map(avis => ({
              commerceId: c.idcommerce,
              commerceNom: c.nom,
              avis: avis.sort((a,b) => new Date(b.datePublication).getTime() - new Date(a.datePublication).getTime())
            }))
          )
        );
        return forkJoin(obsList);
      })
    ).subscribe({
      next: (data: any[]) => {
        this.avisList = data.filter(item => item.avis.length > 0);
        this.avisList.forEach(item => this.totalAvis += item.avis.length);
        this.loading = false;
      },
      error: (err) => {
        console.error("Erreur chargement:", err);
        this.loading = false;
      }
    });
  }

  startDraft(id: string, existing?: string): void {
    this.draftingId = id;
    this.replyText = existing || '';
  }

  envoyerReponse(avis: any): void {
    if (!this.replyText.trim()) return;

    const updateDto = {
      ...avis,
      reponse: this.replyText
    };

    this.avisService.update(avis.idavis, updateDto).subscribe({
      next: () => {
        this.draftingId = null;
        this.replyText = '';
        this.chargerDonnees();
      },
      error: (err) => console.error("Erreur réponse:", err)
    });
  }
}
