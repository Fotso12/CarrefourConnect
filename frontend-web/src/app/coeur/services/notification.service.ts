import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Service pour la gestion des notifications (Port 8084)
 */
const API_URL = 'http://localhost:8084/api/notifications';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  constructor(private readonly http: HttpClient) { }

  /**
   * Récupère les notifications d'un utilisateur
   */
  getByUser(iduser: string): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/user/${iduser}`);
  }

  /**
   * Marque une notification comme lue
   */
  markAsRead(idnotification: string): Observable<any> {
    return this.http.put(`${API_URL}/${idnotification}/lu`, {});
  }

  /**
   * Compte les notifications non lues
   */
  countUnread(iduser: string): Observable<number> {
    return this.http.get<number>(`${API_URL}/user/${iduser}/unread/count`);
  }

  /**
   * Envoie une notification
   */
  send(notification: any): Observable<any> {
    return this.http.post(`${API_URL}/send`, notification);
  }
}
