import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

const API_URL = 'http://localhost:8084/api/offres';

@Injectable({
  providedIn: 'root'
})
export class OffreService {

  constructor(private readonly http: HttpClient) { }

  /**
   * Récupère les offres d'un commerce spécifique
   */
  getByCommerce(commerceId: string): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/commerce/${commerceId}`);
  }

  /**
   * Crée une nouvelle offre spéciale pour un commerce
   */
  create(offre: any): Observable<any> {
    return this.http.post<any>(API_URL, offre);
  }

  /**
   * Met à jour une offre existante
   */
  update(id: string, offre: any): Observable<any> {
    return this.http.put<any>(`${API_URL}/${id}`, offre);
  }

  /**
   * Supprime une offre existante
   */
  delete(id: string): Observable<any> {
    return this.http.delete<any>(`${API_URL}/${id}`);
  }

  /**
   * Récupère toutes les offres actives du système
   */
  getActive(): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/active`);
  }
}
