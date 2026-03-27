import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../../coeur/services/notification.service';
import { AuthService } from '../../../coeur/services/auth.service';

@Component({
  selector: 'app-gestion-notifications-commercant',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './gestion-notifications.html',
  styleUrl: './gestion-notifications.css',
})
export class GestionNotificationsCommercant implements OnInit {
  notifications: any[] = [];
  loading = true;

  constructor(
    private notificationService: NotificationService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.chargerNotifications();
  }

  chargerNotifications(): void {
    const user = this.authService.getUser();
    if (!user?.id) return;
    this.loading = true;
    this.notificationService.getByUser(user.id).subscribe({
      next: (data) => {
        // Trier: non lues d'abord, puis par date décroissante
        this.notifications = data.sort((a, b) => {
          if (a.lu !== b.lu) return a.lu ? 1 : -1;
          return new Date(b.dateEnvoi).getTime() - new Date(a.dateEnvoi).getTime();
        });
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  marquerCommeLu(notif: any): void {
    if (notif.lu) return;
    this.notificationService.markAsRead(notif.idnotification).subscribe({
      next: () => {
        notif.lu = true;
        this.notificationService.notifyRefresh();
        // Réordonner pour mettre les non lues en haut
        this.notifications = [...this.notifications].sort((a, b) => {
          if (a.lu !== b.lu) return a.lu ? 1 : -1;
          return new Date(b.dateEnvoi).getTime() - new Date(a.dateEnvoi).getTime();
        });
      }
    });
  }

  get unreadCount(): number {
    return this.notifications.filter(n => !n.lu).length;
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleString('fr-FR', {
      day: '2-digit', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  typeIcon(type: string): string {
    switch (type) {
      case 'NOUVEAU_COMMERCE': return '🏪';
      case 'VALIDATION': return '✅';
      case 'REJET': return '❌';
      case 'SUSPENSION': return '⚠️';
      case 'OFFRE': return '🎁';
      default: return '🔔';
    }
  }
}
