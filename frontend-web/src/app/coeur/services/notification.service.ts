import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject, BehaviorSubject, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

/**
 * Service pour la gestion des notifications (Port 8084)
 */
const API_URL = 'http://localhost:8084/api/notifications';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  private refreshSubject = new Subject<void>();
  public refresh$ = this.refreshSubject.asObservable();
  private unreadSubject = new BehaviorSubject<number>(0);
  public unread$ = this.unreadSubject.asObservable();

  private stompClient: Client | null = null;
  private subscription: StompSubscription | null | undefined = null;

  constructor(private readonly http: HttpClient) { }

  /**
   * Notifie les abonnés qu'un changement a eu lieu (ex: marquage comme lu)
   */
  notifyRefresh(): void {
    this.refreshSubject.next();
  }

  /**
   * Connect to backend WebSocket STOMP endpoint and subscribe to user-specific topic
   */
  connect(userId: string): void {
    if (!userId) return;
    if (this.stompClient && this.stompClient.connected) return;

    const brokerURL = (environment.wsUrl || 'http://localhost:8084') + '/ws';
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(brokerURL)
    });

    this.stompClient.onConnect = (frame) => {
      // subscribe to personal notifications
      this.subscription = this.stompClient?.subscribe('/topic/notifications/' + userId, (message: IMessage) => {
        try {
          const payload = JSON.parse(message.body);
          // Notify subscribers that a new notification arrived
          this.notifyRefresh();
          // update unread count by fetching server-side count
          this.countUnread(userId).subscribe(count => this.unreadSubject.next(count));
        } catch (e) { console.error('Erreur parsing WS message', e); }
      });

      // subscribe to admin broadcast too
      this.stompClient?.subscribe('/topic/admin/notifications', (message: IMessage) => {
        try { 
          this.notifyRefresh(); 
          this.countUnread(userId).subscribe(count => this.unreadSubject.next(count));
        } catch (e) { /* ignore */ }
      });
    };

    this.stompClient.onStompError = (frame) => {
      console.error('STOMP error', frame);
    };

    this.stompClient.activate();
  }

  disconnect(): void {
    try {
      this.subscription?.unsubscribe();
      this.stompClient?.deactivate();
    } catch (e) { /* ignore */ }
    this.stompClient = null;
  }

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
   * Compte les notifications non lues et met à jour le flux global
   */
  countUnread(iduser: string): Observable<number> {
    return this.http.get<number>(`${API_URL}/user/${iduser}/unread/count`).pipe(
      tap(count => this.unreadSubject.next(count))
    );
  }

  /**
   * Envoie une notification
   */
  send(notification: any): Observable<any> {
    return this.http.post(`${API_URL}/send`, notification);
  }
}
