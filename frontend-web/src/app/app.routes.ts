import { Routes } from '@angular/router';
import { AccueilComponent } from './fonctionnalites/accueil/accueil.component';

export const routes: Routes = [
  { path: '', component: AccueilComponent },
  { 
    path: 'commerce/:id', 
    loadComponent: () => import('./fonctionnalites/accueil/detail-commerce/detail-commerce.component').then(m => m.DetailCommerceComponent)
  },
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
      { 
        path: 'modifier-commerce/:id', 
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
        path: 'dashboard',
        loadComponent: () => import('./fonctionnalites/admin/stats-admin/stats-admin.component').then(m => m.StatsAdminComponent)
      },
      {
        path: 'validation',
        loadComponent: () => import('./fonctionnalites/admin/validation-commerces/validation-commerces.component').then(m => m.ValidationCommercesComponent)
      },
      {
        path: 'utilisateurs',
        loadComponent: () => import('./fonctionnalites/admin/gestion-utilisateurs/gestion-utilisateurs.component').then(m => m.GestionUtilisateursComponent)
      },
      {
        path: 'categories',
        loadComponent: () => import('./fonctionnalites/admin/gestion-categories/gestion-categories.component').then(m => m.GestionCategoriesComponent)
      },
      {
        path: 'abonnements',
        loadComponent: () => import('./fonctionnalites/admin/gestion-abonnements/gestion-abonnements').then(m => m.GestionAbonnements)
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: '' }
];
