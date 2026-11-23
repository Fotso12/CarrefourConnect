package com.carrefourconnect.services.temps_reel;

/**
 * Diffuse une notification temps réel vers une destination STOMP (ex. "/topic/notifications/{id}").
 *
 * Deux implémentations :
 * - {@link LocalNotificationBroadcaster} : envoi direct aux navigateurs connectés à CETTE instance
 *   (suffisant avec un seul pod, utilisé dans les tests) ;
 * - {@link RedisNotificationBroadcaster} : publication dans un canal Redis ; chaque instance abonnée
 *   relaie aux navigateurs qui lui sont connectés. Indispensable dès qu'il y a plusieurs pods.
 */
public interface NotificationBroadcaster {

    void broadcast(String destination, Object payload);
}
