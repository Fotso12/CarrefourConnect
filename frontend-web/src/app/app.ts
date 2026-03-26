import { Component, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './coeur/services/auth.service';

/**
 * Composant racine de l'application
 * Contient le layout global (Header, Footer et RouterOutlet)
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  title = 'CarrefourConnect';
  isLoggedIn: boolean = false;
  showLogoutModal: boolean = false;

  constructor(private readonly authService: AuthService, private readonly router: Router) {}

  get currentUserRole(): string {
    return this.authService.currentUserValue?.role || '';
  }

  ngOnInit(): void {
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
}
