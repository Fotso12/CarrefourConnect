package com.carrefourconnect.services.temps_reel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

/**
 * Diffusion via Redis pub/sub : la notification est PUBLIÉE dans le canal
 * {@value #CHANNEL}. Toutes les instances du backend (y compris celle-ci) y sont abonnées
 * ({@link RedisNotificationRelay}) et la relaient à leurs propres navigateurs connectés.
 * Ainsi, peu importe le pod qui déclenche la notification : tous les clients la reçoivent.
 */
@Component
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
public class RedisNotificationBroadcaster implements NotificationBroadcaster {

    public static final String CHANNEL = "carrefourconnect:notifications";

    private static final Logger log = LoggerFactory.getLogger(RedisNotificationBroadcaster.class);
    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() { };

    private final StringRedisTemplate redis;
    private final JsonMapper jsonMapper;

    public RedisNotificationBroadcaster(StringRedisTemplate redis, JsonMapper jsonMapper) {
        this.redis = redis;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void broadcast(String destination, Object payload) {
        // Conversion avec le JsonMapper de l'application : mêmes formats (dates...) qu'un envoi direct
        Map<String, Object> contenu = jsonMapper.convertValue(payload, MAP);
        String message = jsonMapper.writeValueAsString(new NotificationEnvelope(destination, contenu));
        Long abonnes = redis.convertAndSend(CHANNEL, message);
        log.debug("Notification publiée sur Redis ({} instance(s) abonnée(s)) : {}", abonnes, destination);
    }
}
