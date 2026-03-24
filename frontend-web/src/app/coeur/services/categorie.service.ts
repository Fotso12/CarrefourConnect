import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Service pour la gestion des catégories de commerce (Port 8084)
 */
const API_URL = 'http://localhost:8084/api/categories';

@Injectable({
  providedIn: 'root'
})
export class CategorieService {

  constructor(private http: HttpClient) { }

  /**
   * Récupère toutes les catégories disponibles
   */
  getAll(): Observable<any[]> {
    return this.http.get<any[]>(API_URL);
  }

  /**
   * Crée une nouvelle catégorie
   */
  create(categorie: any): Observable<any> {
    return this.http.post<any>(API_URL, categorie);
  }
}
