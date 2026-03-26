import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UtilisateurService } from '../../../coeur/services/utilisateur.service';
import { ModalComponent } from '../../../partages/composants/modal/modal.component';

@Component({
  selector: 'app-gestion-utilisateurs',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalComponent],
  templateUrl: './gestion-utilisateurs.component.html'
})
export class GestionUtilisateursComponent implements OnInit {
  utilisateurs: any[] = [];
  loading = true;

  // Modal Variables
  selectedUser: any = null;
  showDetailsModal = false;
  showSuspendModal = false;
  motifSuspension = '';

  constructor(private utilisateurService: UtilisateurService) {}

  ngOnInit(): void {
    this.chargerUtilisateurs();
  }

  chargerUtilisateurs(): void {
    this.utilisateurService.getNonAdmins().subscribe({
      next: (data) => {
        this.utilisateurs = data;
        this.loading = false;
      },
      error: (err) => {
        console.error("Erreur chargement utilisateurs:", err);
        this.loading = false;
      }
    });
  }

  openDetails(user: any): void {
    this.selectedUser = user;
    this.showDetailsModal = true;
  }

  closeDetails(): void {
    this.showDetailsModal = false;
    this.selectedUser = null;
  }

  openSuspend(user: any): void {
    this.selectedUser = user;
    this.motifSuspension = '';
    this.showSuspendModal = true;
  }

  closeSuspend(): void {
    this.showSuspendModal = false;
    this.selectedUser = null;
    this.motifSuspension = '';
  }

  confirmerSuspension(): void {
    if (!this.selectedUser || !this.motifSuspension.trim()) return;
    
    this.utilisateurService.suspendre(this.selectedUser.iduser, this.motifSuspension).subscribe({
      next: () => {
        this.chargerUtilisateurs();
        this.closeSuspend();
      },
      error: (err) => console.error("Erreur suspension:", err)
    });
  }

  activer(user: any): void {
    if (confirm(`Voulez-vous vraiment réactiver le compte de ${user.prenom} ${user.nom} ?`)) {
      this.utilisateurService.activer(user.iduser).subscribe({
        next: () => this.chargerUtilisateurs(),
        error: (err) => console.error("Erreur activation:", err)
      });
    }
  }

  getRoleLabel(role: string): string {
    if (!role) return 'Non défini';
    switch (role) {
      case 'ROLE_ADMIN': return 'Administrateur';
      case 'ROLE_COMMERCANT': return 'Commerçant';
      case 'ROLE_VISITEUR': return 'Visiteur';
      case 'ADMIN': return 'Administrateur';
      case 'COMMERCANT': return 'Commerçant';
      case 'VISITEUR': return 'Visiteur';
      default: return role;
    }
  }
}
