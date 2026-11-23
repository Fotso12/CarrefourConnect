package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.NotificationDTO;
import com.carrefourconnect.services.interfaces.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "Gestion des notifications utilisateurs")
@Slf4j
public class NotificationController {

    private final NotificationService service;

    // Ses propres notifications uniquement (ou un administrateur)
    @GetMapping("/user/{iduser}")
    @PreAuthorize("hasRole('ADMIN') or @notificationSecurity.isCurrentUser(#iduser, authentication)")
    @Operation(summary = "Récupère les notifications d'un utilisateur")
    public ResponseEntity<?> getByUser(@PathVariable("iduser") UUID iduser) {
        return ResponseEntity.ok(service.findByUser(iduser));
    }

    @PutMapping("/{idnotification}/lu")
    @PreAuthorize("hasRole('ADMIN') or @notificationSecurity.isOwner(#idnotification, authentication)")
    @Operation(summary = "Marque une notification comme lue")
    public ResponseEntity<?> markAsRead(@PathVariable("idnotification") UUID idnotification) {
        service.markAsRead(idnotification);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{iduser}/unread/count")
    @PreAuthorize("hasRole('ADMIN') or @notificationSecurity.isCurrentUser(#iduser, authentication)")
    @Operation(summary = "Compte le nombre de notifications non lues")
    public ResponseEntity<?> countUnread(@PathVariable("iduser") UUID iduser) {
        return ResponseEntity.ok(service.countUnread(iduser));
    }

    // Envoyer une notification à n'importe quel utilisateur : réservé aux administrateurs
    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Envoie une notification (administrateurs)")
    public ResponseEntity<?> send(@RequestBody NotificationDTO dto) {
        return ResponseEntity.ok(service.send(dto));
    }
}
