import 'zone.js'; // Requis pour Angular (Change Detection)
(window as any).global = window;
import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

/**
 * Point d'entrée principal de l'application
 * Initialise le composant racine (App) avec la configuration globale
 */

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
