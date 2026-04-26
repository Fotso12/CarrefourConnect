import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BaseService } from './base.service';

const API_URL = 'http://localhost:8084/api/avis';

@Injectable({
  providedIn: 'root'
})
export class AvisService extends BaseService<any> {

  constructor(http: HttpClient) {
    super(http, API_URL);
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
}
