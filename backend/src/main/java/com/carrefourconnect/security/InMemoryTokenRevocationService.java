package com.carrefourconnect.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Liste noire en mémoire : valable pour UNE seule instance et perdue au redémarrage.
 * Utilisée quand Redis est désactivé (développement, tests). Avec plusieurs pods, un jeton
 * révoqué sur le pod A resterait accepté par le pod B : d'où la version Redis.
 */
@Component
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryTokenRevocationService implements TokenRevocationService {

    private final Map<String, Instant> revoques = new ConcurrentHashMap<>();

    @Override
    public void revoke(String token, Instant expiresAt) {
        if (expiresAt.isAfter(Instant.now())) {
            revoques.put(TokenRevocationService.fingerprint(token), expiresAt);
        }
    }

    @Override
    public boolean isRevoked(String token) {
        Instant maintenant = Instant.now();
        revoques.values().removeIf(expiration -> expiration.isBefore(maintenant));   // ménage
        return revoques.containsKey(TokenRevocationService.fingerprint(token));
    }
}
