import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

/**
 * URL de base de l'API pour les commerces (Port 8084)
 */
const API_URL = 'http://localhost:8084/api/commerces';

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
    return this.http.get<any>(`${API_URL}/${id}`);
  }

  /**
   * Recherche multicritères (Nom, Categorie, Ville, Proximité)
   */
  rechercher(filtres: any): Observable<any[]> {
    let params = new HttpParams();
    
    // Nettoyage des paramètres pour éviter l'envoi de "undefined" ou null
    if (filtres.nom && filtres.nom.trim()) {
      params = params.set('nom', filtres.nom);
    }
    
    if (filtres.idCategorie && filtres.idCategorie !== 'undefined' && filtres.idCategorie !== 'null') {
      params = params.set('idCategorie', filtres.idCategorie);
    }
    
    if (filtres.ville && filtres.ville.trim()) {
      params = params.set('ville', filtres.ville);
    }
    
    if (filtres.lat && filtres.lon) {
      params = params.set('lat', filtres.lat.toString());
      params = params.set('lon', filtres.lon.toString());
    }
    
    if (filtres.rayon != null && filtres.rayon !== undefined) {
      params = params.set('rayon', filtres.rayon.toString());
    }

    return this.http.get<any[]>(`${API_URL}/rechercher`, { params });
  }

  /**
   * Recherche de proximité pure.
   */
  findNearby(lat: number, lon: number, distance: number): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/proximite`, {
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
    console.debug('[CommerceService] create() payload:', commerce);
    return this.http.post<any>(API_URL, commerce);
  }

  /**
   * Upload un média pour un commerce.
   */
  uploadMedia(file: File, commerceId: string, estPrincipale: boolean = false): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('commerceId', commerceId);
    formData.append('estPrincipale', estPrincipale.toString());
    return this.http.post<any>(`http://localhost:8084/api/medias/upload`, formData);
  }

  /**
   * Recupère les commerces d'un commerçant spécifique.
   */
  getByCommercant(commercantId: string): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/commercant/${commercantId}`);
  }

  /**
   * Met à jour un commerce existant.
   */
  update(id: string, commerce: any): Observable<any> {
    return this.http.put<any>(`${API_URL}/${id}`, commerce);
  }

  /**
   * Met à jour le statut d'un commerce (Validation Admin)
   */
  updateStatut(id: string, statut: string): Observable<any> {
    return this.http.put<any>(`${API_URL}/${id}`, { statut });
  }

  /**
   * Supprime un commerce.
   */
  delete(id: string): Observable<any> {
    return this.http.delete<any>(`${API_URL}/${id}`);
  }
}
