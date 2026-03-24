import { Routes } from '@angular/router';
import { AccueilComponent } from './fonctionnalites/accueil/accueil.component';

export const routes: Routes = [
  { path: '', component: AccueilComponent },
  { 
    path: 'connexion', 
    loadComponent: () => import('./fonctionnalites/auth/connexion/connexion.component').then(m => m.ConnexionComponent) 
  },
  { 
    path: 'inscription-commercant', 
    loadComponent: () => import('./fonctionnalites/auth/inscription-commercant/inscription-commercant.component').then(m => m.InscriptionCommercantComponent) 
  },
  {
    path: 'commercant',
    loadComponent: () => import('./fonctionnalites/commercant/tableau-bord/tableau-bord-commercant.component').then(m => m.TableauBordCommercantComponent),
    children: [
      { 
        path: 'commerces', 
        loadComponent: () => import('./fonctionnalites/commercant/gestion-commerce/gestion-commerce.component').then(m => m.GestionCommerceComponent) 
      },
      { 
        path: 'ajouter-commerce', 
        loadComponent: () => import('./fonctionnalites/commercant/ajouter-commerce/ajouter-commerce.component').then(m => m.AjouterCommerceComponent) 
      },
      { path: '', redirectTo: 'commerces', pathMatch: 'full' }
    ]
  },
  {
    path: 'admin',
    loadComponent: () => import('./fonctionnalites/admin/tableau-bord/tableau-bord-admin.component').then(m => m.TableauBordAdminComponent),
    children: [
      {
        path: 'validation',
        loadComponent: () => import('./fonctionnalites/admin/validation-commerces/validation-commerces.component').then(m => m.ValidationCommercesComponent)
      },
      { path: '', redirectTo: 'validation', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: '' }
];
