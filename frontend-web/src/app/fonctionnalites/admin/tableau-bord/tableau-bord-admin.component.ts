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

  menuItems = [
    { label: 'Vue d\'ensemble', icon: 'M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6z', link: '/admin/dashboard' },
    { label: 'Modération Commerces', icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2', link: '/admin/validation', badge: true },
    { label: 'Utilisateurs', icon: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z', link: '/admin/utilisateurs' },
    { label: 'Gestion Catégories', icon: 'M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z', link: '/admin/categories' },
    { label: 'Abonnements', icon: 'M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z', link: '/admin/abonnements' }
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
    this.commerceService.getByStatut('EN_ATTENTE_VALIDATION').subscribe({
      next: (list) => {
        this.pendingValidationsCount = list.length;
      },
      error: (err) => console.error("Erreur chargement des commerces en attente:", err)
    });
    
    // Charger le nombre de notifications non lues pour l'admin
    const user = this.authService.getUser();
    if (user?.id) {
      this.notificationService.countUnread(user.id).subscribe({
        next: (count: number) => this.unreadCount = count,
        error: () => {}
      });
    }
  }
}
