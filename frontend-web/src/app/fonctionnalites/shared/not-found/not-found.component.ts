import { Component } from '@angular/core';

@Component({
  selector: 'app-not-found',
  standalone: true,
  template: `
    <div style="display:flex;align-items:center;justify-content:center;height:70vh;padding:24px;">
      <div style="max-width:800px;text-align:center;background:#fff;border-radius:16px;padding:40px;box-shadow:0 10px 30px rgba(0,0,0,0.08);">
        <h1 style="font-size:28px;margin:0 0 8px;color:#0f172a;">404 — Page introuvable</h1>
        <p style="color:#64748b;margin-bottom:24px;">La page que vous recherchez n'existe pas ou a été déplacée.</p>
        <a routerLink="/" style="display:inline-block;padding:12px 24px;background:#0b5ed7;color:#fff;border-radius:12px;text-decoration:none;">Retour à l'accueil</a>
      </div>
    </div>
  `
})
export class NotFoundComponent {}
