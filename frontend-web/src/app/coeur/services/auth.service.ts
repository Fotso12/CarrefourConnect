import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map } from 'rxjs/operators';

/**
 * URL de base de l'API d'authentification (Port 8084 configuré)
 */
const AUTH_API = 'http://localhost:8084/api/auth';
const TOKEN_KEY = 'auth-token';
const USER_KEY = 'auth-user';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject: BehaviorSubject<any>;
  public currentUser: Observable<any>;

  /**
   * Initialisation du service avec récupération de l'utilisateur stocké
   */
  constructor(private readonly http: HttpClient) {
    this.currentUserSubject = new BehaviorSubject<any>(JSON.parse(sessionStorage.getItem(USER_KEY) || '{}'));
    this.currentUser = this.currentUserSubject.asObservable();
    
    // Nettoyage de sécurité : si on a des restes de session mais qu'elle est invalide, on vide tout
    if (!this.isLoggedIn() && (sessionStorage.getItem(USER_KEY) || sessionStorage.getItem(TOKEN_KEY))) {
      this.logout();
    }
  }

  public get currentUserValue(): any {
    return this.currentUserSubject.value;
  }

  /**
   * Inscription d'un nouveau commerçant
   */
  registerCommercant(userData: any): Observable<any> {
    return this.http.post<any>(`http://localhost:8084/api/utilisateurs/inscription/commercant`, userData);
  }

  /**
   * Service de connexion : envoie les identifiants au backend
   * Stocke le token et les infos utilisateur en cas de succès
   */
  login(credentials: any): Observable<any> {
    return this.http.post(`${AUTH_API}/login`, {
      email: credentials.email,
      password: credentials.password
    }, httpOptions).pipe(map((user: any) => {
      const token = user && (user.accessToken || user.token || user.jwt);
      if (user && token) {
        this.saveToken(token);
        this.saveUser(user);
        this.currentUserSubject.next(user);
      }
      return user;
    }));
  }

  register(user: any): Observable<any> {
    return this.http.post(`${AUTH_API}/signup`, {
      username: user.username,
      email: user.email,
      password: user.password,
      role: user.role // [ROLE_USER, ROLE_COMMERCANT]
    }, httpOptions);
  }

  logout(): void {
    window.sessionStorage.clear();
    this.currentUserSubject.next({});
  }

  // --- Mot de passe oublié ---
  requestPasswordReset(email: string) {
    return this.http.post(`${AUTH_API}/mot-de-passe-oublie`, { email }, httpOptions);
  }

  verifyResetCode(email: string, code: string) {
    return this.http.post(`${AUTH_API}/verifier-code`, { email, code }, httpOptions);
  }

  resetPassword(email: string, token: string, nouveauMotDePasse: string) {
    return this.http.post(`${AUTH_API}/reinitialiser-mot-de-passe`, { email, token, nouveauMotDePasse }, httpOptions);
  }

  public saveToken(token: string): void {
    window.sessionStorage.removeItem(TOKEN_KEY);
    window.sessionStorage.setItem(TOKEN_KEY, token);
  }

  public getToken(): string | null {
    return window.sessionStorage.getItem(TOKEN_KEY);
  }

  public saveUser(user: any): void {
    window.sessionStorage.removeItem(USER_KEY);
    window.sessionStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  public getUser(): any {
    const user = window.sessionStorage.getItem(USER_KEY);
    if (user) {
      return JSON.parse(user);
    }
    return {};
  }

  public isLoggedIn(): boolean {
    const user = this.getUser();
    const token = this.getToken();
    // Supporte à la fois 'id' et 'iduser' pour plus de robustesse
    const hasUserId = !!(user && (user.id || user.iduser));
    return !!(token && hasUserId);
  }
}
