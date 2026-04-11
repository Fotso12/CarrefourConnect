import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

const API_URL = 'http://localhost:8084/api/avis';

@Injectable({
  providedIn: 'root'
})
export class AvisService {

  constructor(private readonly http: HttpClient) { }

  /**
   * Récupère tous les avis du système (Admin)
   */
  getAll(): Observable<any[]> {
    return this.http.get<any[]>(API_URL);
  }

  /**
   * Récupère tous les avis d'un commerce spécifique
   */
  getByCommerce(commerceId: string): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/commerce/${commerceId}`);
  }

  /**
   * Filtre les avis par statut
   */
  getByStatus(status: string): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/statut/${status}`);
  }

  /**
   * Ajoute un avis
   */
  create(avis: any): Observable<any> {
    return this.http.post<any>(API_URL, avis);
  }

  /**
   * Met à jour un avis (Réponse ou Modération)
   */
  update(id: string, avis: any): Observable<any> {
    return this.http.put<any>(`${API_URL}/${id}`, avis);
  }

  /**
   * Supprime un avis
   */
  delete(id: string): Observable<any> {
    return this.http.delete(`${API_URL}/${id}`);
  }
}
