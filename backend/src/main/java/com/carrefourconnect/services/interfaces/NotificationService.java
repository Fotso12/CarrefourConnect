package com.carrefourconnect.services.interfaces;

import com.carrefourconnect.dtos.NotificationDTO;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    NotificationDTO send(NotificationDTO dto);

    /**
     * Envoie une notification à tous les administrateurs
     */
    void sendToAdmins(NotificationDTO dto);

    List<NotificationDTO> findByUser(UUID iduser);
    void markAsRead(UUID idnotification);
    long countUnread(UUID iduser);
}
