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
    { label: 'Vue d\'ensemble', icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6', link: '/commercant/dashboard' },
    { label: 'Mes Commerces', icon: 'M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4', link: '/commercant/commerces' },
    { label: 'Avis Clients', icon: 'M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z', link: '/commercant/avis' },
    { label: 'Notifications', icon: 'M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9', link: '/commercant/notifications' }
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
