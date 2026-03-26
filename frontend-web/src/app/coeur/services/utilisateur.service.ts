import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Service pour la gestion des utilisateurs (Port 8084)
 */
const API_URL = 'http://localhost:8084/api/utilisateurs';

@Injectable({
  providedIn: 'root'
})
export class UtilisateurService {

  constructor(private readonly http: HttpClient) { }

  /**
   * Récupère tous les utilisateurs
   */
  getAll(): Observable<any[]> {
    return this.http.get<any[]>(API_URL);
  }

  /**
   * Récupère un utilisateur par son ID
   */
  getById(id: string): Observable<any> {
    return this.http.get<any>(`${API_URL}/${id}`);
  }

  /**
   * Met à jour un utilisateur
   */
  update(id: string, utilisateur: any): Observable<any> {
    return this.http.put<any>(`${API_URL}/${id}`, utilisateur);
  }

  /**
   * Supprime un utilisateur
   */
  delete(id: string): Observable<any> {
    return this.http.delete<any>(`${API_URL}/${id}`);
  }

  /**
   * Récupère tous les utilisateurs sauf les administrateurs
   */
  getNonAdmins(): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/non-admins`);
  }

  /**
   * Suspend un utilisateur avec un motif
   */
  suspendre(id: string, motif: string): Observable<any> {
    const params = new URLSearchParams();
    params.set('motif', motif);
    return this.http.put(`${API_URL}/${id}/suspendre?${params.toString()}`, {});
  }

  /**
   * Active un utilisateur suspendu
   */
  activer(id: string): Observable<any> {
    return this.http.put(`${API_URL}/${id}/activer`, {});
  }
}
