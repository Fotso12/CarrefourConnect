import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, Routes } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';
import { AuthInterceptor } from './coeur/intercepteurs/auth.interceptor';

import { AccueilComponent } from './fonctionnalites/accueil/accueil.component';

import { routes } from './app.routes';

/**
 * Configuration globale de l'application Angular
 * Inclut le routage, le client HTTP et l'intercepteur JWT
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    // Configuration du client HTTP avec support des intercepteurs
    provideHttpClient(withInterceptorsFromDi()),
    // Enregistrement de l'intercepteur d'authentification
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
  ]
};
