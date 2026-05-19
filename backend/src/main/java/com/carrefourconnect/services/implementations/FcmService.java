package com.carrefourconnect.services.implementations;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class FcmService {

    /**
     * Envoie une notification push à un token spécifique.
     */
    public void sendPushNotification(String token, String title, String body, Map<String, String> data) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Notification push envoyée avec succès: {}", response);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification push: {}", e.getMessage());
        }
    }

    /**
     * Envoie une notification à un sujet (topic).
     * Utile pour notifier tous les favoris d'un commerce par exemple.
     */
    public void sendTopicNotification(String topic, String title, String body, Map<String, String> data) {
        try {
            Message message = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Notification de sujet '{}' envoyée avec succès: {}", topic, response);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification de sujet: {}", e.getMessage());
        }
    }
}
