package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.CategorieDTO;
import com.carrefourconnect.services.interfaces.CategorieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categorie", description = "Gestion des types de commerces")
@Slf4j
public class CategorieController {

    private final CategorieService service;

    @GetMapping
    @Operation(summary = "Liste toutes les catégories")
    public ResponseEntity<?> getAll() {
        log.info("Requête pour lister toutes les catégories");
        try {
            return ResponseEntity.ok(service.findAll());
        } catch (Exception e) {
            log.error("Erreur lister catégories: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère une catégorie par son ID")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        log.info("Requête pour récupérer la catégorie ID: {}", id);
        try {
            CategorieDTO dto = service.findById(id);
            return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur récupération catégorie {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping
    @Operation(summary = "Crée une nouvelle catégorie")
    public ResponseEntity<?> save(@RequestBody CategorieDTO dto) {
        log.info("Requête de création de catégorie: {}", dto.getNom());
        try {
            return new ResponseEntity<>(service.save(dto), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Erreur création catégorie {}: {}", dto.getNom(), e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour une catégorie")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody CategorieDTO dto) {
        log.info("Requête de mise à jour catégorie ID: {}", id);
        try {
            CategorieDTO updated = service.update(id, dto);
            return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur mise à jour catégorie {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime une catégorie")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        log.info("Requête de suppression catégorie ID: {}", id);
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Erreur suppression catégorie {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/recherche")
    @Operation(summary = "Recherche une catégorie par nom")
    public ResponseEntity<?> searchByName(@RequestParam String nom) {
        log.info("Recherche catégorie par nom: {}", nom);
        try {
            return ResponseEntity.ok(service.findByNom(nom));
        } catch (Exception e) {
            log.error("Erreur recherche catégorie: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
