package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.PaiementDTO;
import com.carrefourconnect.services.interfaces.PaiementService;
import com.carrefourconnect.utils.enums.StatutPaiement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
@Tag(name = "Paiement", description = "Gestion des transactions de paiement")
@Slf4j
public class PaiementController {

    private final PaiementService service;

    @GetMapping
    @Operation(summary = "Liste tous les paiements")
    public ResponseEntity<?> getAll() {
        log.info("Requête pour lister tous les paiements");
        try {
            return ResponseEntity.ok(service.findAll());
        } catch (Exception e) {
            log.error("Erreur lister paiements: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère un paiement par son ID")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        log.info("Requête pour récupérer le paiement ID: {}", id);
        try {
            PaiementDTO dto = service.findById(id);
            return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur récupération paiement {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping
    @Operation(summary = "Enregistre un nouveau paiement")
    public ResponseEntity<?> save(@RequestBody PaiementDTO dto) {
        log.info("Nouveau paiement pour l'abonnement: {}", dto.getIdabonnement());
        try {
            return new ResponseEntity<>(service.save(dto), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Erreur création paiement: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour un paiement")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody PaiementDTO dto) {
        log.info("Mise à jour du paiement ID: {}", id);
        try {
            PaiementDTO updated = service.update(id, dto);
            return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur mise à jour paiement {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un paiement")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        log.info("Suppression du paiement ID: {}", id);
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Erreur suppression paiement {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/abonnement/{abonnementId}")
    @Operation(summary = "Liste les paiements d'un abonnement")
    public ResponseEntity<?> getByAbonnement(@PathVariable UUID abonnementId) {
        log.info("Récupération paiements pour l'abonnement: {}", abonnementId);
        try {
            return ResponseEntity.ok(service.findByAbonnement(abonnementId));
        } catch (Exception e) {
            log.error("Erreur paiements abonnement {}: {}", abonnementId, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/reference/{reference}")
    @Operation(summary = "Récupère un paiement par sa référence")
    public ResponseEntity<?> getByReference(@PathVariable String reference) {
        log.info("Recherche paiement par référence: {}", reference);
        try {
            PaiementDTO dto = service.findByReference(reference);
            return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur recherche référence {}: {}", reference, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/statut/{statut}")
    @Operation(summary = "Filtre les paiements par statut")
    public ResponseEntity<?> getByStatut(@PathVariable StatutPaiement statut) {
        log.info("Filtrage des paiements par statut: {}", statut);
        try {
            return ResponseEntity.ok(service.findByStatut(statut));
        } catch (Exception e) {
            log.error("Erreur filtrage paiements statut {}: {}", statut, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
