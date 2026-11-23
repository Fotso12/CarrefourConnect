package com.carrefourconnect.services.temps_reel;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Diffusion des notifications : publication dans Redis puis relais par chaque instance.
 */
class NotificationBroadcasterTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void localEnvoieDirectementAuBrokerStomp() {
        SimpMessagingTemplate stomp = mock(SimpMessagingTemplate.class);
        new LocalNotificationBroadcaster(stomp).broadcast("/topic/notifications/42", Map.of("titre", "Bonjour"));

        verify(stomp).convertAndSend(eq("/topic/notifications/42"), any(Object.class));
    }

    @Test
    void redisPublieDestinationEtContenuDansLeCanal() throws Exception {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        new RedisNotificationBroadcaster(redis, jsonMapper)
                .broadcast("/topic/admin/notifications", Map.of("titre", "Nouveau commerce"));

        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        verify(redis).convertAndSend(eq(RedisNotificationBroadcaster.CHANNEL), message.capture());
        NotificationEnvelope envelope = jsonMapper.readValue(message.getValue(), NotificationEnvelope.class);
        assertThat(envelope.destination()).isEqualTo("/topic/admin/notifications");
        assertThat(envelope.payload()).containsEntry("titre", "Nouveau commerce");
    }

    @Test
    void leRelaisTransmetLeMessageRecuAuxClientsLocaux() {
        SimpMessagingTemplate stomp = mock(SimpMessagingTemplate.class);
        String json = "{\"destination\":\"/topic/notifications/42\",\"payload\":{\"titre\":\"Bonjour\"}}";

        new RedisNotificationRelay(stomp, jsonMapper).onMessage(
                new DefaultMessage(RedisNotificationBroadcaster.CHANNEL.getBytes(StandardCharsets.UTF_8),
                        json.getBytes(StandardCharsets.UTF_8)), null);

        ArgumentCaptor<Object> contenu = ArgumentCaptor.forClass(Object.class);
        verify(stomp).convertAndSend(eq("/topic/notifications/42"), contenu.capture());
        assertThat((Map<String, Object>) contenu.getValue()).containsEntry("titre", "Bonjour");
    }

    @Test
    void leRelaisIgnoreUnMessageIllisibleSansPlanter() {
        SimpMessagingTemplate stomp = mock(SimpMessagingTemplate.class);

        new RedisNotificationRelay(stomp, jsonMapper).onMessage(
                new DefaultMessage(new byte[0], "pas du json".getBytes(StandardCharsets.UTF_8)), null);

        verify(stomp, never()).convertAndSend(anyString(), any(Object.class));
    }
}
