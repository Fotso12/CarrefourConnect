package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.LocalisationDTO;
import com.carrefourconnect.services.interfaces.LocalisationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/localisations")
@RequiredArgsConstructor
@Tag(name = "Localisation", description = "Gestion des emplacements géographiques")
@Slf4j
public class LocalisationController {

    private final LocalisationService service;

    @GetMapping
    @Operation(summary = "Liste toutes les localisations")
    public ResponseEntity<?> getAll() {
        log.info("Requête pour lister toutes les localisations");
        try {
            return ResponseEntity.ok(service.findAll());
        } catch (Exception e) {
            log.error("Erreur lister localisations: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère une localisation par son ID")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        log.info("Requête pour récupérer la localisation ID: {}", id);
        try {
            LocalisationDTO dto = service.findById(id);
            return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur récupération localisation {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping
    @Operation(summary = "Enregistre une nouvelle localisation")
    public ResponseEntity<?> save(@RequestBody LocalisationDTO dto) {
        log.info("Nouvelle localisation pour commerce: {}", dto.getIdcommerce());
        try {
            return new ResponseEntity<>(service.save(dto), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Erreur création localisation commerce {}: {}", dto.getIdcommerce(), e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour une localisation")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody LocalisationDTO dto) {
        log.info("Mise à jour de la localisation ID: {}", id);
        try {
            LocalisationDTO updated = service.update(id, dto);
            return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur mise à jour localisation {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime une localisation")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        log.info("Suppression de la localisation ID: {}", id);
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Erreur suppression localisation {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/commerce/{commerceId}")
    @Operation(summary = "Liste les localisations d'un commerce")
    public ResponseEntity<?> getByCommerce(@PathVariable UUID commerceId) {
        log.info("Récupération localisations commerce: {}", commerceId);
        try {
            return ResponseEntity.ok(service.findByCommerce(commerceId));
        } catch (Exception e) {
            log.error("Erreur localisations commerce {}: {}", commerceId, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/ville/{ville}")
    @Operation(summary = "Liste les localisations d'une ville")
    public ResponseEntity<?> getByVille(@PathVariable String ville) {
        log.info("Récupération localisations ville: {}", ville);
        try {
            return ResponseEntity.ok(service.findByVille(ville));
        } catch (Exception e) {
            log.error("Erreur localisations ville {}: {}", ville, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
