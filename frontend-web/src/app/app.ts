import { Component, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './coeur/services/auth.service';

/**
 * Composant racine de l'application
 * Contient le layout global (Header, Footer et RouterOutlet)
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  title = 'CarrefourConnect';
  isLoggedIn: boolean = false;
  showLogoutModal: boolean = false;
  showMobileMenu: boolean = false;
  isDashboardMode: boolean = false;

  constructor(private readonly authService: AuthService, private readonly router: Router) {}

  get currentUserRole(): string {
    return this.authService.currentUserValue?.role || '';
  }

  ngOnInit(): void {
    console.log('[App] Démarrage... URL actuelle:', window.location.pathname);
    console.log('[App] Rôle actuel:', this.currentUserRole);
    
    // Pister tous les changements de route pour le diagnostic et le layout
    this.router.events.subscribe(event => {
      if (event.constructor.name === 'NavigationEnd') {
        const url = (event as any).url;
        console.log('[App] Navigation terminée vers:', url);
        this.isDashboardMode = url.includes('/admin') || url.includes('/commercant');
      }
    });

    // S'abonner à l'utilisateur actuel pour mettre à jour l'état du header
    this.authService.currentUser.subscribe(() => {
      this.isLoggedIn = this.authService.isLoggedIn();
    });
  }

  openLogout(): void {
    this.showLogoutModal = true;
  }

  confirmLogout(): void {
    this.authService.logout();
    this.showLogoutModal = false;
    this.router.navigate(['/']);
  }

  cancelLogout(): void {
    this.showLogoutModal = false;
  }

  toggleMobileMenu(): void {
    this.showMobileMenu = !this.showMobileMenu;
  }

  closeMobileMenu(): void {
    this.showMobileMenu = false;
  }
}
