package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.MediaDTO;
import com.carrefourconnect.services.interfaces.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/medias")
@RequiredArgsConstructor
@Tag(name = "Media", description = "Gestion des fichiers multimédias")
@Slf4j
public class MediaController {

    private final MediaService service;

    @GetMapping
    @Operation(summary = "Liste tous les médias")
    public ResponseEntity<?> getAll() {
        log.info("Requête pour lister tous les médias");
        try {
            return ResponseEntity.ok(service.findAll());
        } catch (Exception e) {
            log.error("Erreur lister médias: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère un média par son ID")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        log.info("Requête pour récupérer le média ID: {}", id);
        try {
            MediaDTO dto = service.findById(id);
            return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur récupération média {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping
    @Operation(summary = "Ajoute un nouveau média")
    public ResponseEntity<?> save(@RequestBody MediaDTO dto) {
        log.info("Requête d'ajout média: {}", dto.getNom());
        try {
            return new ResponseEntity<>(service.save(dto), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Erreur ajout média: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour un média")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody MediaDTO dto) {
        log.info("Requête de mise à jour média ID: {}", id);
        try {
            MediaDTO updated = service.update(id, dto);
            return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur mise à jour média {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un média")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        log.info("Requête de suppression média ID: {}", id);
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Erreur suppression média {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/commerce/{commerceId}")
    @Operation(summary = "Liste les médias d'un commerce")
    public ResponseEntity<?> getByCommerce(@PathVariable UUID commerceId) {
        log.info("Récupération médias pour commerce: {}", commerceId);
        try {
            return ResponseEntity.ok(service.findByCommerce(commerceId));
        } catch (Exception e) {
            log.error("Erreur médias commerce {}: {}", commerceId, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
