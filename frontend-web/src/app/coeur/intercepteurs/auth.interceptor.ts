import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private readonly authService: AuthService) { }

  /**
   * Intercepte chaque requête HTTP sortante
   * Si un token JWT est présent, il est ajouté au header Authorization
   */
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let authReq = req;
    const token = this.authService.getToken();
    // Debug log: show whether a token was found and what will be attached
    try {
      console.debug('[AuthInterceptor] token present:', Boolean(token));
      if (token) {
        // Ajout du préfixe Bearer requis par Spring Security
        authReq = req.clone({ headers: req.headers.set('Authorization', 'Bearer ' + token) });
        console.debug('[AuthInterceptor] attaching Authorization header');
      } else {
        console.debug('[AuthInterceptor] no token to attach');
      }
    } catch (e) {
      // Defensive: do not break requests if logging fails
      console.warn('[AuthInterceptor] logging error', e);
    }

    return next.handle(authReq);
  }
}
