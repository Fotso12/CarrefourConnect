package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.AbonnementDTO;
import com.carrefourconnect.dtos.PlanConfigDTO;
import com.carrefourconnect.services.interfaces.AbonnementService;
import com.carrefourconnect.utils.enums.StatutAbonnement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Contrôleur REST pour la gestion des abonnements.
 * Permet de créer, consulter, modifier et filtrer les forfaits des commerçants.
 */
@RestController
@RequestMapping("/api/abonnements")
@RequiredArgsConstructor
@Tag(name = "Abonnement", description = "Gestion des forfaits et abonnements commerçants")
@Slf4j
public class AbonnementController {

    private final AbonnementService service;

    /**
     * Retourne la liste de tous les abonnements enregistrés.
     */
    @GetMapping
    @Operation(summary = "Liste tous les abonnements")
    public ResponseEntity<?> getAll() {
        log.info("Requête pour lister tous les abonnements");
        try {
            return ResponseEntity.ok(service.findAll());
        } catch (Exception e) {
            log.error("Erreur lister abonnements: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Récupère un abonnement par son identifiant unique.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Récupère un abonnement par son ID")
    public ResponseEntity<?> getById(@PathVariable("id") UUID id) {
        log.info("Requête pour récupérer l'abonnement ID: {}", id);
        try {
            AbonnementDTO dto = service.findById(id);
            return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur récupération abonnement {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retourne la configuration (droits et prix) du plan de référence pour un type donné.
     * Utilisé par le frontend pour afficher les features et appliquer les restrictions.
     *
     * @param type Le type de plan : BASIQUE, PREMIUM ou GOLD
     */
    @GetMapping("/config/{type}")
    @Operation(summary = "Retourne la configuration d'un plan par son type")
    public ResponseEntity<?> getConfigParType(@PathVariable("type") String type) {
        log.info("Requête de configuration pour le plan: {}", type);
        try {
            AbonnementDTO config = service.findConfigParType(type.toUpperCase());
            return config != null ? ResponseEntity.ok(config) : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.error("Type de plan invalide: {}", type);
            return new ResponseEntity<>("Type de plan invalide: " + type, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Erreur récupération config plan {}: {}", type, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Met à jour la configuration complète (prix + droits) de tous les abonnements
     * d'un type donné. Action réservée à l'admin.
     *
     * @param type   Le type de plan : BASIQUE, PREMIUM ou GOLD
     * @param config Les nouvelles valeurs de configuration
     */
    @PutMapping("/config/{type}")
    @Operation(summary = "Met à jour la configuration d'un plan (admin)")
    public ResponseEntity<?> updateConfigParType(
            @PathVariable("type") String type,
            @RequestBody PlanConfigDTO config) {
        log.info("Mise à jour de la configuration du plan: {}", type);
        try {
            service.updateConfigParType(type.toUpperCase(), config);
            return ResponseEntity.ok("Configuration du plan " + type + " mise à jour avec succès.");
        } catch (IllegalArgumentException e) {
            log.error("Type de plan invalide: {}", type);
            return new ResponseEntity<>("Type de plan invalide: " + type, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Erreur mise à jour config plan {}: {}", type, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Crée un nouvel abonnement pour un commerçant.
     */
    @PostMapping
    @Operation(summary = "Crée un nouvel abonnement")
    public ResponseEntity<?> save(@RequestBody AbonnementDTO dto) {
        log.info("Requête de création d'abonnement pour: {}", dto.getType());
        try {
            return new ResponseEntity<>(service.save(dto), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Erreur création abonnement: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Met à jour les données d'un abonnement existant.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Met à jour un abonnement")
    public ResponseEntity<?> update(@PathVariable("id") UUID id, @RequestBody AbonnementDTO dto) {
        log.info("Requête de mise à jour d'abonnement ID: {}", id);
        try {
            AbonnementDTO updated = service.update(id, dto);
            return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur mise à jour abonnement {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Supprime définitivement un abonnement du système.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un abonnement")
    public ResponseEntity<?> delete(@PathVariable("id") UUID id) {
        log.info("Requête de suppression d'abonnement ID: {}", id);
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Erreur suppression abonnement {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Met à jour le prix de tous les abonnements d'un type donné (BASIQUE, PREMIUM, GOLD).
     */
    @PutMapping("/tarif/{type}")
    @Operation(summary = "Met à jour le prix de tous les abonnements d'un type donné")
    public ResponseEntity<?> updatePrixParType(
            @PathVariable("type") String type,
            @RequestBody java.util.Map<String, java.math.BigDecimal> body) {
        log.info("Mise à jour du prix pour le type: {}", type);
        try {
            java.math.BigDecimal prix = body.get("prix");
            if (prix == null) {
                return new ResponseEntity<>("Le champ 'prix' est requis", HttpStatus.BAD_REQUEST);
            }
            service.updatePrixParType(type, prix);
            return ResponseEntity.ok("Prix mis à jour avec succès pour le type: " + type);
        } catch (IllegalArgumentException e) {
            log.error("Type d'abonnement invalide: {}", type);
            return new ResponseEntity<>("Type d'abonnement invalide: " + type, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Erreur mise à jour prix type {}: {}", type, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Récupère l'historique des abonnements pour un commerçant (tous ses commerces).
     */
    @GetMapping("/commercant/{userId}")
    @Operation(summary = "Récupère l'historique d'abonnement d'un commerçant")
    public ResponseEntity<?> getByCommercant(@PathVariable("userId") UUID userId) {
        log.info("Requête historique abonnement pour commerçant: {}", userId);
        try {
            return ResponseEntity.ok(service.findByCommercant(userId));
        } catch (Exception e) {
            log.error("Erreur historique abonnement commerçant {}: {}", userId, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
