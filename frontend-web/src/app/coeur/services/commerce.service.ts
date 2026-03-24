import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

/**
 * URL de base de l'API pour les commerces (Port 8084)
 */
const API_URL = 'http://localhost:8084/api/commerces/';

@Injectable({
  providedIn: 'root'
})
export class CommerceService {

  constructor(private readonly http: HttpClient, private readonly authService: AuthService) { }

  /**
   * Récupère tous les commerces.
   */
  getAll(): Observable<any[]> {
    return this.http.get<any[]>(API_URL);
  }

  /**
   * Récupère un commerce par son ID.
   */
  getById(id: string): Observable<any> {
    return this.http.get<any>(API_URL + id);
  }

  /**
   * Recherche multicritères (Nom, Categorie, Ville, Proximité)
   */
  rechercher(filtres: any): Observable<any[]> {
    let params = new HttpParams();
    // Construction dynamique des paramètres de requête
    if (filtres.nom) params = params.set('nom', filtres.nom);
    if (filtres.idCategorie) params = params.set('idCategorie', filtres.idCategorie);
    if (filtres.ville) params = params.set('ville', filtres.ville);
    if (filtres.lat) params = params.set('lat', filtres.lat.toString());
    if (filtres.lon) params = params.set('lon', filtres.lon.toString());
    if (filtres.rayon) params = params.set('rayon', filtres.rayon.toString());

    return this.http.get<any[]>(API_URL + 'rechercher', { params });
  }

  /**
   * Recherche de proximité pure.
   */
  findNearby(lat: number, lon: number, distance: number): Observable<any[]> {
    return this.http.get<any[]>(API_URL + 'proximite', {
      params: new HttpParams()
        .set('lat', lat.toString())
        .set('lon', lon.toString())
        .set('distance', distance.toString())
    });
  }

  /**
   * Crée un nouveau commerce pour le commerçant connecté.
   */
  create(commerce: any): Observable<any> {
    // Prefer the registered AuthInterceptor to handle headers. As a fallback,
    // attach Authorization header here using AuthService if present.
    const token = this.authService.getToken();
    const headers = token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : undefined;
    return this.http.post<any>(API_URL, commerce, headers ? { headers } : {});
  }
}
