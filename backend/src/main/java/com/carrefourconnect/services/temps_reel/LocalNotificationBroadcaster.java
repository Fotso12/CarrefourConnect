package com.carrefourconnect.services.temps_reel;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Diffusion locale : le message n'atteint que les navigateurs connectés à cette instance.
 * Utilisée quand Redis est désactivé (app.redis.enabled=false, valeur par défaut).
 */
@Component
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "false", matchIfMissing = true)
public class LocalNotificationBroadcaster implements NotificationBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public LocalNotificationBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void broadcast(String destination, Object payload) {
        messagingTemplate.convertAndSend(destination, payload);
    }
}
