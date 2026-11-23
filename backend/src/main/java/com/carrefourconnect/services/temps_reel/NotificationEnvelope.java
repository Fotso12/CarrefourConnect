package com.carrefourconnect.services.temps_reel;

import java.util.Map;

/**
 * Message échangé entre les instances via Redis : la destination STOMP et le contenu
 * de la notification (déjà converti en JSON générique).
 */
public record NotificationEnvelope(String destination, Map<String, Object> payload) {
}
