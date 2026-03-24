import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Service pour la gestion des notifications (Alertes, Avis, Validation)
 */
const API_URL = 'http://localhost:8084/api/notifications';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  constructor(private http: HttpClient) { }

  /**
   * Récupère les notifications d'un utilisateur
   */
  getByUser(userId: string): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/utilisateur/${userId}`);
  }

  /**
   * Marqué une notification comme lue
   */
  markAsRead(id: string): Observable<any> {
    return this.http.put(`${API_URL}/${id}/lu`, {});
  }
}
