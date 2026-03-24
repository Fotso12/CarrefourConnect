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

  ngOnInit(): void {
    // Subscribe to current user to update header state
    this.authService.currentUser.subscribe(user => {
      // If user object has keys, consider logged in
      this.isLoggedIn = user && Object.keys(user).length > 0;
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
