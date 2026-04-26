package com.carrefourconnect.controllers;

import com.carrefourconnect.services.implementations.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint utilitaire pour tester la configuration SMTP en développement.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Test", description = "Endpoints utilitaires pour l'admin (tests)")
@Slf4j
public class TestEmailController {

    private final EmailService emailService;

    @GetMapping("/test-email")
    @Operation(summary = "Envoie un email de test au commerçant et une notification admin")
    public ResponseEntity<?> testEmail(@RequestParam(value = "email", required = false) String email) {
        String cible = (email == null || email.isBlank()) ? "test@local" : email;
        try {
            // Envoi vers l'admin (via le mécanisme existant pour nouveau commerce)
            emailService.envoyerNotificationNouveauCommerce("CommerceTest", "Test Commerçant", cible);

            // Envoi d'un email de validation test au destinataire fourni
            emailService.envoyerEmailValidationCommerce(cible, "CommerceTest");

            log.info("Email de test envoyé vers: {}", cible);
            return ResponseEntity.ok("Emails de test envoyés");
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi d'email de test: {}", e.getMessage());
            return ResponseEntity.status(500).body("Erreur envoi email: " + e.getMessage());
        }
    }
}
