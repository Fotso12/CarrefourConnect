import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet, Router } from '@angular/router';
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
    { label: 'Mes Commerces', icon: 'fa-solid fa-store', link: '/commercant/commerces' },
    { label: 'Avis Clients', icon: 'fa-solid fa-comments', link: '/commercant/avis' },
    { label: 'Notifications', icon: 'fa-solid fa-bell', link: '/commercant/notifications' }
  ];

  isSidebarOpen = false;
  showLogoutConfirm = false;
  currentUser: any = {};
  displayName: string = '';
  displayRole: string = '';
  private readonly notificationService = inject(NotificationService);
  unreadCount$ = this.notificationService.unread$;

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.isSidebarOpen = window.innerWidth > 1024;
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

      const userId = this.currentUser.iduser || this.currentUser.id;
      if (userId) {
        this.loadUnreadCount();
        // Connect websocket for real-time notifications
        this.notificationService.connect(userId);
      }
    });

    // S'abonner aux rafraîchissements (ex: après marquage comme lu)
    this.notificationService.refresh$.subscribe(() => {
      this.loadUnreadCount();
    });
  }

  loadUnreadCount(): void {
    const userId = this.currentUser.iduser || this.currentUser.id;
    if (userId) {
      this.notificationService.countUnread(userId).subscribe({
        next: (count) => console.log('Badge notifications:', count),
        error: (err) => console.error('Erreur notifications:', err)
      });
    }
  }

  toggleSidebar() {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  onNavItemClick() {
    if (window.innerWidth <= 1024) {
      this.isSidebarOpen = false;
    }
  }

  goToProfile(): void {
    // Navigate to the commercant profile page
    this.router.navigate(['/commercant/profil']);
    // close sidebar on small screens for better UX
    if (window.innerWidth <= 1024) this.isSidebarOpen = false;
  }

  logout(): void {
    this.authService.logout();
    this.showLogoutConfirm = false;
    this.router.navigate(['/']);
  }
}
