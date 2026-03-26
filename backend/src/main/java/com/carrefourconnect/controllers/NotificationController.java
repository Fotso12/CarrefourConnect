package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.NotificationDTO;
import com.carrefourconnect.services.interfaces.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "Gestion des notifications utilisateurs")
@Slf4j
public class NotificationController {

    private final NotificationService service;

    @GetMapping("/user/{iduser}")
    @Operation(summary = "Récupère les notifications d'un utilisateur")
    public ResponseEntity<?> getByUser(@PathVariable("iduser") UUID iduser) {
        return ResponseEntity.ok(service.findByUser(iduser));
    }

    @PutMapping("/{idnotification}/lu")
    @Operation(summary = "Marque une notification comme lue")
    public ResponseEntity<?> markAsRead(@PathVariable("idnotification") UUID idnotification) {
        service.markAsRead(idnotification);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{iduser}/unread/count")
    @Operation(summary = "Compte le nombre de notifications non lues")
    public ResponseEntity<?> countUnread(@PathVariable("iduser") UUID iduser) {
        return ResponseEntity.ok(service.countUnread(iduser));
    }

    @PostMapping("/send")
    @Operation(summary = "Envoie une notification (interne/admin)")
    public ResponseEntity<?> send(@RequestBody NotificationDTO dto) {
        return ResponseEntity.ok(service.send(dto));
    }
}
