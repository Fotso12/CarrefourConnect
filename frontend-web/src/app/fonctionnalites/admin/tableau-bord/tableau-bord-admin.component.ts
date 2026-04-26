import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BehaviorSubject } from 'rxjs';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { CommerceService } from '../../../coeur/services/commerce.service';
import { AuthService } from '../../../coeur/services/auth.service';
import { NotificationService } from '../../../coeur/services/notification.service';
import { HttpClient } from '@angular/common/http';

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
  private readonly notificationService = inject(NotificationService);
  pendingValidationsCount$ = new BehaviorSubject<number>(0);
  unreadCount$ = this.notificationService.unread$;
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
      private router: Router,
      private http: HttpClient
  ) {
    const savedColor = localStorage.getItem('admin_accent_color');
    if (savedColor) this.accentColor = savedColor;
  }


  ngOnInit(): void {
    // Appel direct pour charger les compteurs au démarrage
    this.loadCounts();

    this.authService.currentUser.subscribe(user => {
      this.currentUser = user || {};
      const userId = this.currentUser.iduser || this.currentUser.id;
      if (userId) {
        this.loadCounts();
        // Connect websocket for real-time notifications
        this.notificationService.connect(userId);
      }
    });

    // S'abonner aux rafraîchissements
    this.notificationService.refresh$.subscribe(() => {
      this.loadCounts();
    });

    // Aussi s'abonner pour des rafraîchissements immédiats depuis d'autres composants
    this.notificationService.refresh$.subscribe(() => this.loadCounts());

    setInterval(() => {
        if (this.currentUser && this.currentUser.iduser) {
            this.loadCounts();
        }
    }, 30000); // 30s
  }

  loadCounts(): void {
    // Charger le nombre de validations en attente et le nombre de notifications non lues
    // Compteur validations
    this.commerceService.getByStatut('EN_ATTENTE_VALIDATION').subscribe({
      next: (list) => {
        const count = Array.isArray(list) ? list.length : 0;
        this.pendingValidationsCount$.next(count);
      },
      error: (err) => console.error('Erreur chargement validations:', err)
    });

    // Compteur notifications non lues pour l'admin (utilisateur courant)
    const userId = this.currentUser.iduser || this.currentUser.id;
    if (userId) {
      this.notificationService.countUnread(userId).subscribe();
    }
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
