package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.AbonnementDTO;
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

@RestController
@RequestMapping("/api/abonnements")
@RequiredArgsConstructor
@Tag(name = "Abonnement", description = "Gestion des forfaits et abonnements commerçants")
@Slf4j
public class AbonnementController {

    private final AbonnementService service;

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

    @GetMapping("/{id}")
    @Operation(summary = "Récupère un abonnement par son ID")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        log.info("Requête pour récupérer l'abonnement ID: {}", id);
        try {
            AbonnementDTO dto = service.findById(id);
            return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur récupération abonnement {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

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

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour un abonnement")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody AbonnementDTO dto) {
        log.info("Requête de mise à jour d'abonnement ID: {}", id);
        try {
            AbonnementDTO updated = service.update(id, dto);
            return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur mise à jour abonnement {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un abonnement")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        log.info("Requête de suppression d'abonnement ID: {}", id);
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Erreur suppression abonnement {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/statut/{statut}")
    @Operation(summary = "Filtre les abonnements par statut")
    public ResponseEntity<?> getByStatut(@PathVariable StatutAbonnement statut) {
        log.info("Filtrage des abonnements par statut: {}", statut);
        try {
            return ResponseEntity.ok(service.findByStatut(statut));
        } catch (Exception e) {
            log.error("Erreur filtrage abonnements statut {}: {}", statut, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
