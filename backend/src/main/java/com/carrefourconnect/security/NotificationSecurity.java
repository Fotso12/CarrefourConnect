package com.carrefourconnect.security;

import com.carrefourconnect.repositories.NotificationRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Règles d'accès aux notifications, utilisées dans les @PreAuthorize de NotificationController
 * (bean nommé "notificationSecurity").
 *
 * Être connecté ne suffit pas : un utilisateur ne doit lire ou modifier QUE ses propres
 * notifications. Sinon, il suffirait de changer l'identifiant dans l'URL pour accéder à celles
 * des autres (faille de type IDOR : Insecure Direct Object Reference).
 */
@Component("notificationSecurity")
public class NotificationSecurity {

    private final NotificationRepository repository;

    public NotificationSecurity(NotificationRepository repository) {
        this.repository = repository;
    }

    /** L'utilisateur connecté est-il celui dont l'identifiant figure dans l'URL ? */
    public boolean isCurrentUser(UUID iduser, Authentication authentication) {
        return iduser != null
                && authentication != null
                && authentication.getPrincipal() instanceof UserDetailsImpl user
                && iduser.equals(user.getId());
    }

    /** La notification appartient-elle à l'utilisateur connecté ? */
    public boolean isOwner(UUID idnotification, Authentication authentication) {
        if (idnotification == null
                || authentication == null
                || !(authentication.getPrincipal() instanceof UserDetailsImpl user)) {
            return false;
        }
        return repository.findById(idnotification)
                .map(notification -> notification.getDestinataire() != null
                        && user.getId().equals(notification.getDestinataire().getIduser()))
                .orElse(false);
    }
}
