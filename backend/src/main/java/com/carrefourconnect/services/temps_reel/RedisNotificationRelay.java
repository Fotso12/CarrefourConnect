package com.carrefourconnect.services.temps_reel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

/**
 * Abonné au canal Redis des notifications : chaque message reçu est relayé aux navigateurs
 * connectés à CETTE instance, via le broker STOMP local.
 * L'abonnement est déclaré dans {@link com.carrefourconnect.config.RedisConfig}.
 */
@Component
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
public class RedisNotificationRelay implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(RedisNotificationRelay.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final JsonMapper jsonMapper;

    public RedisNotificationRelay(SimpMessagingTemplate messagingTemplate, JsonMapper jsonMapper) {
        this.messagingTemplate = messagingTemplate;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            NotificationEnvelope envelope = jsonMapper.readValue(json, NotificationEnvelope.class);
            // (Object) : sans ce cast, la Map serait prise pour des EN-TÊTES de message (autre surcharge)
            messagingTemplate.convertAndSend(envelope.destination(), (Object) envelope.payload());
            log.debug("Notification relayée aux clients locaux : {}", envelope.destination());
        } catch (Exception e) {
            // Un message illisible ne doit jamais arrêter l'abonnement Redis
            log.warn("Notification Redis ignorée (message invalide) : {}", e.getMessage());
        }
    }
}
