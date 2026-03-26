import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Service pour la gestion des abonnements (Port 8084)
 */
const API_URL = 'http://localhost:8084/api/abonnements';

@Injectable({
  providedIn: 'root'
})
export class AbonnementService {

  constructor(private readonly http: HttpClient) { }

  /**
   * Récupère tous les abonnements
   */
  getAll(): Observable<any[]> {
    return this.http.get<any[]>(API_URL);
  }

  /**
   * Récupère un abonnement par son ID
   */
  getById(id: string): Observable<any> {
    return this.http.get<any>(`${API_URL}/${id}`);
  }

  /**
   * Crée un nouvel abonnement
   */
  create(abonnement: any): Observable<any> {
    return this.http.post(API_URL, abonnement);
  }

  /**
   * Met à jour un abonnement
   */
  update(id: string, abonnement: any): Observable<any> {
    return this.http.put(`${API_URL}/${id}`, abonnement);
  }

  /**
   * Supprime un abonnement
   */
  delete(id: string): Observable<any> {
    return this.http.delete(`${API_URL}/${id}`);
  }
}
