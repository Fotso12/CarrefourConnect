import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div *ngIf="isOpen" class="fixed inset-0 z-[100] flex items-center justify-center p-4">
      <!-- Backdrop -->
      <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm transition-opacity" (click)="close()"></div>
      
      <!-- Modal Content -->
      <div class="relative bg-white rounded-[32px] shadow-2xl shadow-slate-200/50 w-full max-w-lg overflow-hidden transform transition-all animate-in fade-in zoom-in duration-300">
        <!-- Close Button -->
        <button (click)="close()" class="absolute top-6 right-6 p-2 rounded-xl text-slate-400 hover:bg-slate-50 hover:text-slate-600 transition-colors">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M6 18L18 6M6 6l12 12"/>
          </svg>
        </button>

        <div class="p-10">
          <div class="flex items-center gap-4 mb-8">
            <div [class]="'p-3 rounded-2xl bg-gradient-to-br ' + iconBg">
              <span class="text-white">
                <ng-content select="[modal-icon]"></ng-content>
              </span>
            </div>
            <div>
              <h2 class="text-2xl font-black text-slate-800 leading-tight">{{ title }}</h2>
              <p class="text-sm font-bold text-slate-400 mt-0.5">{{ subtitle }}</p>
            </div>
          </div>

          <div class="space-y-6">
            <ng-content select="[modal-body]"></ng-content>
          </div>

          <div class="flex gap-4 mt-10">
            <button (click)="close()" class="flex-1 bg-slate-50 text-slate-500 font-black py-4 rounded-2xl hover:bg-slate-100 transition-all duration-300">
              Annuler
            </button>
            <button (click)="confirm()" [class]="'flex-1 text-white font-black py-4 rounded-2xl transition-all duration-300 transform hover:scale-[1.02] active:scale-95 shadow-lg ' + btnBg">
              {{ confirmLabel }}
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host { display: block; }
    .animate-in {
      animation: modal-in 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
    }
    @keyframes modal-in {
      from { opacity: 0; transform: scale(0.95) translateY(10px); }
      to { opacity: 1; transform: scale(1) translateY(0); }
    }
  `]
})
export class ModalComponent {
  @Input() isOpen = false;
  @Input() title = 'Titre';
  @Input() subtitle = 'Sous-titre';
  @Input() iconBg = 'from-[#00ADEF] to-[#00709B]';
  @Input() btnBg = 'bg-[#00ADEF] shadow-[#00ADEF]/20';
  @Input() confirmLabel = 'Confirmer';

  @Output() onConfirm = new EventEmitter<void>();
  @Output() onCancel = new EventEmitter<void>();

  close() {
    this.isOpen = false;
    this.onCancel.emit();
  }

  confirm() {
    this.onConfirm.emit();
  }
}
