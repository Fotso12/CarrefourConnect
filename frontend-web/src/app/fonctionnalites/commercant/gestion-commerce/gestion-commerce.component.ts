import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CommerceService } from '../../../coeur/services/commerce.service';

/**
 * Composant pour la liste des commerces d'un commerçant
 */
@Component({
  selector: 'app-gestion-commerce',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './gestion-commerce.component.html',
  styleUrl: './gestion-commerce.component.css'
})
export class GestionCommerceComponent implements OnInit {
  commerces: any[] = [];
  loading = true;

  constructor(private readonly commerceService: CommerceService) {}

  ngOnInit(): void {
    // Dans une vraie app, on filtrerait par l'ID du commerçant connecté
    // Pour la démo, on récupère tout ou on simule
    this.commerceService.getAll().subscribe({
      next: (data) => {
        this.commerces = data;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }
}
