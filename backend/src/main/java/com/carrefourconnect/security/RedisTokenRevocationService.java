package com.carrefourconnect.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Liste noire partagée dans Redis : un jeton révoqué sur un pod est refusé par TOUS les pods.
 *
 * Chaque jeton révoqué devient une clé "carrefourconnect:jwt-revoque:{empreinte}" avec une durée
 * de vie (TTL) égale au temps qui lui restait : Redis la supprime tout seul à l'expiration du
 * jeton, la liste ne grossit donc jamais indéfiniment.
 *
 * Choix assumé si Redis est injoignable : on laisse passer (fail-open) en le journalisant. Refuser
 * tous les jetons (fail-closed) serait plus sûr, mais une panne de Redis couperait alors
 * l'application entière ; les jetons expirent de toute façon au bout de 24 h.
 */
@Component
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
public class RedisTokenRevocationService implements TokenRevocationService {

    static final String PREFIX = "carrefourconnect:jwt-revoque:";

    private static final Logger log = LoggerFactory.getLogger(RedisTokenRevocationService.class);

    private final StringRedisTemplate redis;

    public RedisTokenRevocationService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void revoke(String token, Instant expiresAt) {
        Duration restant = Duration.between(Instant.now(), expiresAt);
        if (restant.isNegative() || restant.isZero()) {
            return;   // déjà expiré : rien à mémoriser
        }
        redis.opsForValue().set(PREFIX + TokenRevocationService.fingerprint(token), "1", restant);
    }

    @Override
    public boolean isRevoked(String token) {
        try {
            return Boolean.TRUE.equals(redis.hasKey(PREFIX + TokenRevocationService.fingerprint(token)));
        } catch (RuntimeException e) {
            log.warn("Redis injoignable, vérification de révocation ignorée : {}", e.getMessage());
            return false;
        }
    }
}
