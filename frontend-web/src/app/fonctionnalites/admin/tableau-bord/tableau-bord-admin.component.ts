import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { CommerceService } from '../../../coeur/services/commerce.service';
import { AuthService } from '../../../coeur/services/auth.service';
import { NotificationService } from '../../../coeur/services/notification.service';

/**
 * Dashboard principal pour l'administration
 */
@Component({
  selector: 'app-tableau-bord-admin',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './tableau-bord-admin.component.html',
  styleUrl: './tableau-bord-admin.component.css'
})
export class TableauBordAdminComponent implements OnInit {
  pendingValidationsCount = 0;
  unreadCount = 0;
  currentUser: any = {};
  isSidebarOpen = false;
  showLogoutConfirm = false;
  showSettings = false;
  accentColor = '#034d92';
  colors = ['#034d92', '#f97316', '#10b981', '#6366f1', '#ec4899', '#f59e0b', '#06b6d4', '#8b5cf6'];

  menuItems = [
    { label: 'Vue d\'ensemble', icon: 'fa-solid fa-chart-line', link: '/admin/dashboard' },
    { label: 'Modération Commerces', icon: 'fa-solid fa-user-shield', link: '/admin/validation', badge: true },
    { label: 'Utilisateurs', icon: 'fa-solid fa-users', link: '/admin/utilisateurs' },
    { label: 'Catégories', icon: 'fa-solid fa-layer-group', link: '/admin/categories' },
    { label: 'Abonnements', icon: 'fa-solid fa-wallet', link: '/admin/abonnements' },
    { label: 'Avis Clients', icon: 'fa-solid fa-comment-dots', link: '/admin/avis' }
  ];

  constructor(
      private commerceService: CommerceService,
      private authService: AuthService,
      private notificationService: NotificationService,
      private router: Router
  ) {
    const savedColor = localStorage.getItem('admin_accent_color');
    if (savedColor) this.accentColor = savedColor;
  }

  ngOnInit(): void {
    // Appel direct pour charger les compteurs au démarrage
    this.loadCounts();

    this.authService.currentUser.subscribe(user => {
      this.currentUser = user || {};
      if (this.currentUser.iduser) {
        this.loadCounts();
      }
    });

    // S'abonner aux rafraîchissements
    this.notificationService.refresh$.subscribe(() => {
      this.loadCounts();
    });

    setInterval(() => {
        if (this.currentUser && this.currentUser.iduser) {
            this.loadCounts();
        }
    }, 30000); // 30s
  }

  loadCounts(): void {
    // ... (rest of the method unchanged, but we add the toggle below)
  }

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  goToProfile(): void {
    this.router.navigate(['/admin/profil']);
    if (window.innerWidth <= 1024) this.isSidebarOpen = false;
  }

  logout(): void {
    this.authService.logout();
    this.showLogoutConfirm = false;
    this.router.navigate(['/']);
  }

  setAccentColor(color: string): void {
    this.accentColor = color;
    localStorage.setItem('admin_accent_color', color);
  }
}
