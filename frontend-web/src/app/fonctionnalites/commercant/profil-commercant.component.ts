import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../coeur/services/auth.service';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Component({
  selector: 'app-profil-commercant',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profil-commercant.component.html',
  styleUrls: ['./profil-commercant.component.css']
})
export class ProfilCommercantComponent {
  commerce: any = {};
  user: any = {};
  message = '';
  loading = false;
  showModal = false;
  showConfirm = false;
  confirmMessage = '';
  private pendingAction: (() => void) | null = null;
  editModel: any = {};

  private apiBase = 'http://localhost:8084/api';
  private currentUserId: string | null = null;

  constructor(private auth: AuthService, private router: Router, private http: HttpClient) {
    const u = this.auth.currentUserValue || {};
    this.commerce = u.commerce || null;
    const id = u?.iduser || u?.id;
    if (id) this.loadUser(id);
  }

  private loadUser(id: string) {
    this.loading = true;
    const token = this.auth.getToken();
    const headers = new HttpHeaders({ 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) });

    // Load basic user info
    this.http.get(`${this.apiBase}/utilisateurs/${id}`, { headers }).subscribe({
      next: (res: any) => {
        this.user = res || {};
        this.currentUserId = res?.iduser || res?.id || this.currentUserId;
      },
      error: (err) => { console.warn('Impossible de charger utilisateur:', err); }
    });

    // Also fetch commerces for other flows, but we won't display the commerce name on profile
    this.http.get(`${this.apiBase}/commerces/commercant/${id}`, { headers }).subscribe({
      next: (res: any) => {
        if (Array.isArray(res) && res.length > 0) {
          this.commerce = res[0];
        } else if (res) {
          this.commerce = res;
        } else {
          this.commerce = {};
        }
        this.loading = false;
      },
      error: (err) => { console.error('Erreur chargement commerces du commercant', err); this.loading = false; }
    });
  }

  edit() {
    this.showModal = true;
    this.editModel = {
      prenom: this.user?.prenom || '',
      nom: this.user?.nom || this.user?.displayName || '',
      email: this.user?.email || '',
      status: this.user?.status || '',
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    };
  }

  save() {
    const uid = this.currentUserId || this.auth.currentUserValue?.iduser || this.auth.currentUserValue?.id;
    if (!uid) { this.message = 'Impossible de déterminer l\'utilisateur.'; return; }

    if (!this.isFormValid()) { this.message = 'Veuillez compléter correctement le formulaire.'; return; }

    const newPassword = this.editModel?.newPassword;
    const profileChanged = (this.editModel.prenom && this.editModel.prenom !== this.user?.prenom)
      || (this.editModel.nom && this.editModel.nom !== this.user?.nom)
      || (this.editModel.email && this.editModel.email !== this.user?.email);

    const token = this.auth.getToken();
    const headers = new HttpHeaders({ 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) });

    // If only changing password and profile didn't change, call password endpoint directly
    if (newPassword && !profileChanged) {
      const body = { ancienMotDePasse: this.editModel.oldPassword, nouveauMotDePasse: newPassword };
      this.loading = true;
      this.http.put(`${this.apiBase}/utilisateurs/${uid}/mot-de-passe`, body, { headers }).subscribe({
        next: () => { this.finalizeSave('Mot de passe mis à jour.'); },
        error: (err) => { console.error('Erreur changement mot de passe', err); this.message = err?.error?.message || 'Erreur changement mot de passe'; this.loading = false; }
      });
      return;
    }

    const payload: any = {
      prenom: this.editModel.prenom || this.user?.prenom,
      nom: this.editModel.nom || this.user?.nom,
      email: this.editModel.email || this.user?.email
    };

    this.loading = true;
    // Update user
    this.http.put(`${this.apiBase}/utilisateurs/${uid}`, payload, { headers }).subscribe({
      next: (res: any) => {
        this.user = res;
        // Password change if requested
        if (newPassword) {
          const body = { ancienMotDePasse: this.editModel.oldPassword, nouveauMotDePasse: newPassword };
          this.http.put(`${this.apiBase}/utilisateurs/${uid}/mot-de-passe`, body, { headers }).subscribe({
            next: () => { this.finalizeSave('Profil utilisateur mis à jour.'); },
            error: (err) => { console.error('Erreur changement mot de passe', err); this.message = err?.error?.message || 'Erreur changement mot de passe'; this.loading = false; }
          });
        } else {
          this.finalizeSave('Profil utilisateur mis à jour.');
        }
      },
      error: (err) => { console.error('Erreur mise à jour utilisateur', err); this.loading = false; this.message = 'Erreur mise à jour utilisateur.'; }
    });
  }

  requestSave() {
    if (this.editModel?.newPassword) {
      this.confirmMessage = 'Vous êtes sur le point de modifier votre mot de passe. Confirmez-vous ?';
    } else {
      this.confirmMessage = 'Confirmez-vous la mise à jour de votre profil utilisateur ?';
    }
    this.pendingAction = () => this.save();
    this.showConfirm = true;
  }

  confirmSave() {
    this.showConfirm = false;
    if (this.pendingAction) {
      const act = this.pendingAction;
      this.pendingAction = null;
      act();
    }
  }

  cancelConfirm() {
    this.showConfirm = false;
    this.pendingAction = null;
  }

  finalizeSave(msg: string) {
    this.showModal = false;
    this.loading = false;
    this.showMessage(msg);
  }

  isFormValid(): boolean {
    // Accept either profile update (prenom+nom+email) or password-only update
    const newPassword = this.editModel?.newPassword;
    if (newPassword) {
      if (!this.editModel.oldPassword) return false;
      if (newPassword !== this.editModel.confirmPassword) return false;
      if ((newPassword || '').length < 6) return false;
      return true;
    }

    const prenom = this.editModel?.prenom?.toString().trim();
    const nom = this.editModel?.nom?.toString().trim();
    const email = this.editModel?.email?.toString().trim();
    if (!prenom || !nom || !email) return false;
    const re = /^\S+@\S+\.\S+$/;
    if (!re.test(email)) return false;
    return true;
  }

  showMessage(msg: string) {
    this.message = msg;
    setTimeout(() => { this.message = ''; }, 4500);
  }
}
