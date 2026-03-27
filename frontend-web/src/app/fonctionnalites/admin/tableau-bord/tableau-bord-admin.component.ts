import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
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

  menuItems = [
    { label: 'Vue d\'ensemble', icon: 'fa-solid fa-chart-line', link: '/admin/dashboard' },
    { label: 'Modération Commerces', icon: 'fa-solid fa-user-shield', link: '/admin/validation', badge: true },
    { label: 'Utilisateurs', icon: 'fa-solid fa-users', link: '/admin/utilisateurs' },
    { label: 'Gestion Catégories', icon: 'fa-solid fa-tags', link: '/admin/categories' },
    { label: 'Abonnements', icon: 'fa-solid fa-credit-card', link: '/admin/abonnements' }
  ];

  constructor(
      private commerceService: CommerceService,
      private authService: AuthService,
      private notificationService: NotificationService
  ) {}

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
}
