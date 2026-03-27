import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../coeur/services/auth.service';
import { NotificationService } from '../../../coeur/services/notification.service';

/**
 * Composant principal du Tableau de Bord Commerçant
 */
@Component({
  selector: 'app-tableau-bord-commercant',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './tableau-bord-commercant.component.html',
  styleUrl: './tableau-bord-commercant.component.css'
})
export class TableauBordCommercantComponent implements OnInit {
  menuItems = [
    { label: 'Mes Commerces', icon: 'M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4', link: '/commercant/commerces' },
    { label: 'Avis Clients', icon: 'M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z', link: '/commercant/avis' }
  ];

  isSidebarOpen = true;
  currentUser: any = {};
  displayName: string = '';
  displayRole: string = '';
  unreadCount = 0;

  constructor(
    private readonly authService: AuthService,
    private readonly notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.authService.currentUser.subscribe(user => {
      this.currentUser = user || {};
      // Prefer nom + prenom, fallback to email
      if (this.currentUser && (this.currentUser.nom || this.currentUser.prenom)) {
        this.displayName = `${this.currentUser.prenom || ''} ${this.currentUser.nom || ''}`.trim();
      } else if (this.currentUser && this.currentUser.email) {
        this.displayName = this.currentUser.email;
      } else {
        this.displayName = '';
      }
      this.displayRole = this.currentUser.role || '';

      if (this.currentUser.iduser) {
        this.loadUnreadCount();
      }
    });

    // S'abonner aux rafraîchissements (ex: après marquage comme lu)
    this.notificationService.refresh$.subscribe(() => {
      if (this.currentUser.iduser) {
        this.loadUnreadCount();
      }
    });
  }

  loadUnreadCount(): void {
    this.notificationService.countUnread(this.currentUser.iduser).subscribe({
      next: (count) => this.unreadCount = count,
      error: (err) => console.error("Erreur chargement notifications:", err)
    });
  }

  toggleSidebar() {
    this.isSidebarOpen = !this.isSidebarOpen;
  }
}
