import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../coeur/services/auth.service';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Component({
  selector: 'app-profil-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profil-admin.component.html',
  styleUrls: ['./profil-admin.component.css']
})
export class ProfilAdminComponent {
  user: any = null;
  message = '';
  editing = false;
  showModal = false;
  showConfirm = false;
  confirmMessage = '';
  private pendingAction: (() => void) | null = null;
  editModel: any = {};
  loading = false;

  private apiBase = 'http://localhost:8084/api';

  constructor(private auth: AuthService, private router: Router, private http: HttpClient) {
    this.user = this.auth.currentUserValue || {};
  }

  ngOnInit(): void {
    // Subscribe to auth changes and load fresh user data from backend when possible
    this.auth.currentUser.subscribe((u: any) => {
      this.user = u || {};
      const id = u?.iduser || u?.id;
      if (id) this.loadUser(id);
    });
    // initial load if value present
    const initial = this.auth.currentUserValue;
    const id = initial?.iduser || initial?.id;
    if (id) this.loadUser(id);
  }

  private loadUser(id: string) {
    this.loading = true;
    const token = this.auth.getToken();
    const headers = new HttpHeaders({ 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) });
    this.http.get(`${this.apiBase}/utilisateurs/${id}`, { headers }).subscribe({
      next: (res: any) => { this.user = res; this.loading = false; },
      error: (err) => { console.error('Erreur chargement utilisateur', err); this.loading = false; }
    });
  }

  edit() {
    // open modal and populate the edit model
    this.showModal = true;
    this.editModel = { displayName: this.user.displayName || '', email: this.user.email || '', phone: this.user.phone || '', city: this.user.city || '', oldPassword: '', newPassword: '', confirmPassword: '' };
  }

  save() {
    const id = this.user?.iduser || this.user?.id;
    if (!id) { this.message = 'Impossible de déterminer l\'utilisateur.'; return; }
    // Client-side validation
    if (!this.isFormValid()) {
      this.message = 'Veuillez corriger les erreurs dans le formulaire.';
      return;
    }

    const newPassword = this.editModel?.newPassword;
    const profileChanged = (this.editModel.displayName && this.editModel.displayName !== this.user?.displayName)
      || (this.editModel.email && this.editModel.email !== this.user?.email)
      || (this.editModel.phone && this.editModel.phone !== this.user?.phone)
      || (this.editModel.city && this.editModel.city !== this.user?.city);

    const token = this.auth.getToken();
    const headers = new HttpHeaders({ 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) });

    // If only changing password and no profile fields changed, call password endpoint directly
    if (newPassword && !profileChanged) {
      const body = { ancienMotDePasse: this.editModel.oldPassword, nouveauMotDePasse: newPassword };
      this.loading = true;
      this.http.put(`${this.apiBase}/utilisateurs/${id}/mot-de-passe`, body, { headers }).subscribe({
        next: () => { this.finalizeSave('Mot de passe mis à jour.'); },
        error: (err) => { console.error('Erreur changement de mot de passe', err); this.message = err?.error?.message || 'Ancien mot de passe invalide.'; this.loading = false; }
      });
      return;
    }

    // Prepare payload merging edited fields with existing user values to avoid accidental nulling
    const payload: any = {
      nom: this.editModel.displayName || this.user?.displayName,
      email: this.editModel.email || this.user?.email,
      telephone: this.editModel.phone || this.user?.phone,
      ville: this.editModel.city || this.user?.city
    };

    this.loading = true;
    // Update profile first
    this.http.put(`${this.apiBase}/utilisateurs/${id}`, payload, { headers }).subscribe({
      next: (res: any) => {
        this.user = res;
        try { this.auth.saveUser(res); } catch(e) { }
        if (newPassword) {
          const body = { ancienMotDePasse: this.editModel.oldPassword, nouveauMotDePasse: newPassword };
          this.http.put(`${this.apiBase}/utilisateurs/${id}/mot-de-passe`, body, { headers }).subscribe({
            next: () => { this.finalizeSave('Profil et mot de passe mis à jour.'); },
            error: (err) => { console.error('Erreur changement de mot de passe', err); this.message = err?.error?.message || 'Ancien mot de passe invalide.'; this.loading = false; }
          });
        } else {
          this.finalizeSave('Profil mis à jour.');
        }
      },
      error: (err) => { console.error(err); this.message = 'Erreur mise à jour'; this.loading = false; }
    });
  }

  // open confirmation modal before performing save
  requestSave() {
    // if changing password include explicit warning
    if (this.editModel?.newPassword) {
      this.confirmMessage = 'Vous êtes sur le point de modifier votre mot de passe. Confirmez-vous ?';
    } else {
      this.confirmMessage = 'Confirmez-vous la mise à jour de votre profil ?';
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
    this.editing = false;
    this.loading = false;
    this.showMessage(msg);
  }

  showMessage(msg: string) {
    this.message = msg;
    setTimeout(() => { this.message = ''; }, 4500);
  }

  isFormValid(): boolean {
    // Allow two valid modes:
    // - profile update: name+email required and valid
    // - password-only update: oldPassword + newPassword+confirm valid
    const newPassword = this.editModel?.newPassword;
    const confirm = this.editModel?.confirmPassword;
    const oldPwd = this.editModel?.oldPassword;

    // password-change flow (allowed even if profile fields empty)
    if (newPassword) {
      if (!oldPwd) return false;
      if (newPassword !== confirm) return false;
      if ((newPassword || '').length < 6) return false;
      return true;
    }

    // otherwise require profile fields
    const name = this.editModel?.displayName?.toString().trim();
    const email = this.editModel?.email?.toString().trim();
    if (!name || !email) return false;
    const re = /^\S+@\S+\.\S+$/;
    if (!re.test(email)) return false;
    return true;
  }
  
}

