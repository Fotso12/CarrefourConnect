package com.carrefourconnect.config;

import com.carrefourconnect.services.temps_reel.RedisNotificationBroadcaster;
import com.carrefourconnect.services.temps_reel.RedisNotificationRelay;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Abonnement de l'instance au canal Redis des notifications (pub/sub).
 * La connexion elle-même (hôte, port, mot de passe) est configurée par Spring Boot à partir des
 * variables SPRING_DATA_REDIS_HOST, SPRING_DATA_REDIS_PORT et SPRING_DATA_REDIS_PASSWORD.
 */
@Configuration
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
public class RedisConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory,
                                                                       RedisNotificationRelay relay) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(relay, new ChannelTopic(RedisNotificationBroadcaster.CHANNEL));
        return container;
    }
}
