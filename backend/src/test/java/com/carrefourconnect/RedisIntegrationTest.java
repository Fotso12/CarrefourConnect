package com.carrefourconnect;

import com.carrefourconnect.security.RedisTokenRevocationService;
import com.carrefourconnect.security.TokenRevocationService;
import com.carrefourconnect.services.interfaces.NotificationService;
import com.carrefourconnect.services.temps_reel.NotificationBroadcaster;
import com.carrefourconnect.services.temps_reel.RedisNotificationBroadcaster;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Tests contre un VRAI Redis, démarré dans un conteneur Docker par Testcontainers
 * (même image que dans Docker Compose et Kubernetes). @ServiceConnection fournit
 * automatiquement l'hôte et le port du conteneur à Spring Boot.
 * Ignoré automatiquement si Docker n'est pas disponible.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = "app.redis.enabled=true")
@ActiveProfiles("test")
class RedisIntegrationTest {

    @Container
    @ServiceConnection
    static final RedisContainer REDIS = new RedisContainer(DockerImageName.parse("redis:8.10.3-alpine"));

    @Autowired
    private TokenRevocationService tokenRevocationService;

    @Autowired
    private NotificationBroadcaster broadcaster;

    @Autowired
    private StringRedisTemplate redis;

    // On observe ce que le relais Redis transmet au broker STOMP local
    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void lesImplementationsRedisSontActives() {
        assertThat(tokenRevocationService).isInstanceOf(RedisTokenRevocationService.class);
        assertThat(broadcaster).isInstanceOf(RedisNotificationBroadcaster.class);
    }

    @Test
    void unJetonRevoqueEstStockeAvecUneDureeDeVieEtRefuse() {
        tokenRevocationService.revoke("jeton-integration", Instant.now().plus(Duration.ofMinutes(5)));

        assertThat(tokenRevocationService.isRevoked("jeton-integration")).isTrue();
        assertThat(tokenRevocationService.isRevoked("autre-jeton")).isFalse();
        String cle = "carrefourconnect:jwt-revoque:" + TokenRevocationService.fingerprint("jeton-integration");
        Long ttl = redis.getExpire(cle);
        assertThat(ttl).isBetween(250L, 300L);   // Redis supprimera la clé tout seul à l'expiration
    }

    @Test
    void uneNotificationPublieeRevientParRedisEtEstRelayeeAuxClientsLocaux() {
        broadcaster.broadcast("/topic/admin/notifications", Map.of("titre", "Nouveau commerce à valider"));

        // Aller-retour complet : publication -> canal Redis -> abonnement -> broker STOMP local
        verify(messagingTemplate, timeout(5000)).convertAndSend(eq("/topic/admin/notifications"), any(Object.class));
    }
}
