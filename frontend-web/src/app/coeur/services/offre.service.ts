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
}
