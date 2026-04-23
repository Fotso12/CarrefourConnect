import { Routes } from '@angular/router';
import { AccueilComponent } from './fonctionnalites/accueil/accueil.component';

export const routes: Routes = [
  { path: '', component: AccueilComponent, pathMatch: 'full' },
  { 
    path: 'commerce/:id', 
    loadComponent: () => import('./fonctionnalites/accueil/detail-commerce/detail-commerce.component').then(m => m.DetailCommerceComponent)
  },
  { 
    path: 'a-propos', 
    loadComponent: () => import('./fonctionnalites/accueil/a-propos/a-propos.component').then(m => m.AproposComponent) 
  },
  { 
    path: 'connexion', 
    loadComponent: () => import('./fonctionnalites/auth/connexion/connexion.component').then(m => m.ConnexionComponent) 
  },
  {
    path: 'auth/mot-de-passe-oublie',
    loadComponent: () => import('./fonctionnalites/auth/mot-de-passe-oublie/mot-de-passe-oublie.component').then(m => m.MotDePasseOublieComponent)
  },
  {
    path: 'auth/verifier-code',
    loadComponent: () => import('./fonctionnalites/auth/verifier-code/verifier-code.component').then(m => m.VerifierCodeComponent)
  },
  {
    path: 'auth/reinitialiser-mot-de-passe',
    loadComponent: () => import('./fonctionnalites/auth/reinitialiser-mot-de-passe/reinitialiser-mot-de-passe.component').then(m => m.ReinitialiserMotDePasseComponent)
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
      {
        path: 'notifications',
        loadComponent: () => import('./fonctionnalites/commercant/gestion-notifications/gestion-notifications').then(m => m.GestionNotificationsCommercant)
      },
      { 
        path: 'avis', 
        loadComponent: () => import('./fonctionnalites/commercant/gestion-avis/gestion-avis.component').then(m => m.GestionAvisCommercantComponent) 
      },
      {
        path: 'profil',
        loadComponent: () => import('./fonctionnalites/commercant/profil-commercant.component').then(m => m.ProfilCommercantComponent)
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
      {
        path: 'notifications',
        loadComponent: () => import('./fonctionnalites/admin/gestion-notifications/gestion-notifications').then(m => m.GestionNotifications)
      },
      {
        path: 'avis',
        loadComponent: () => import('./fonctionnalites/admin/gestion-avis/gestion-avis.component').then(m => m.GestionAvisAdminComponent)
      },
      {
        path: 'profil',
        loadComponent: () => import('./fonctionnalites/admin/profil-admin.component').then(m => m.ProfilAdminComponent)
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  // Profil routes moved into their parent dashboards so sidebar/navbar remain visible
  { path: '**', loadComponent: () => import('./fonctionnalites/shared/not-found/not-found.component').then(m => m.NotFoundComponent) }
];
