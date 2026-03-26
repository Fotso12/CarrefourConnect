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
   * Récupère tous les avis d'un commerce spécifique
   */
  getByCommerce(commerceId: string): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/commerce/${commerceId}`);
  }

  /**
   * Ajoute un avis
   */
  create(avis: any): Observable<any> {
    return this.http.post<any>(API_URL, avis);
  }
}
