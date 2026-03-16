package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.AvisDTO;
import com.carrefourconnect.services.interfaces.AvisService;
import com.carrefourconnect.utils.enums.StatutAvis;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/avis")
@RequiredArgsConstructor
@Tag(name = "Avis", description = "Gestion des avis et notes des visiteurs")
@Slf4j
public class AvisController {

    private final AvisService service;

    @GetMapping
    @Operation(summary = "Liste tous les avis")
    public ResponseEntity<?> getAll() {
        log.info("Requête pour lister tous les avis");
        try {
            return ResponseEntity.ok(service.findAll());
        } catch (Exception e) {
            log.error("Erreur lister avis: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère un avis par son ID")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        log.info("Requête pour récupérer l'avis ID: {}", id);
        try {
            AvisDTO dto = service.findById(id);
            return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur récupération avis {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping
    @Operation(summary = "Enregistre un nouvel avis")
    public ResponseEntity<?> save(@RequestBody AvisDTO dto) {
        log.info("Nouvel avis pour le commerce: {}", dto.getIdcommerce());
        try {
            return new ResponseEntity<>(service.save(dto), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Erreur création avis commerce {}: {}", dto.getIdcommerce(), e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour un avis")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody AvisDTO dto) {
        log.info("Mise à jour de l'avis ID: {}", id);
        try {
            AvisDTO updated = service.update(id, dto);
            return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur mise à jour avis {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un avis")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        log.info("Suppression de l'avis ID: {}", id);
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Erreur suppression avis {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/commerce/{commerceId}")
    @Operation(summary = "Liste les avis d'un commerce")
    public ResponseEntity<?> getByCommerce(@PathVariable UUID commerceId) {
        log.info("Récupération des avis pour le commerce: {}", commerceId);
        try {
            return ResponseEntity.ok(service.findByCommerce(commerceId));
        } catch (Exception e) {
            log.error("Erreur avis commerce {}: {}", commerceId, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/visiteur/{visiteurId}")
    @Operation(summary = "Liste les avis d'un visiteur")
    public ResponseEntity<?> getByVisiteur(@PathVariable UUID visiteurId) {
        log.info("Récupération des avis pour le visiteur: {}", visiteurId);
        try {
            return ResponseEntity.ok(service.findByVisiteur(visiteurId));
        } catch (Exception e) {
            log.error("Erreur avis visiteur {}: {}", visiteurId, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/statut/{status}")
    @Operation(summary = "Filtre les avis par statut")
    public ResponseEntity<?> getByStatus(@PathVariable StatutAvis status) {
        log.info("Filtrage des avis par statut: {}", status);
        try {
            return ResponseEntity.ok(service.findByStatus(status));
        } catch (Exception e) {
            log.error("Erreur filtrage avis statut {}: {}", status, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
