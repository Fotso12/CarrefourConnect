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

/**
 * Contrôleur REST pour la gestion des catégories de commerces.
 * Permet de créer, consulter et gérer les types de commerces (Restaurant, Boulangerie, etc.).
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categorie", description = "Gestion des types de commerces")
@Slf4j
public class CategorieController {

    private final CategorieService service;

    /**
     * Retourne la liste de toutes les catégories disponibles.
     *
     * @return 200 OK avec la liste des catégories, ou 400 en cas d'erreur.
     */
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

    /**
     * Récupère une catégorie par son identifiant unique.
     *
     * @param id L'UUID de la catégorie recherchée.
     * @return 200 OK avec la catégorie, 404 si introuvable, ou 400 en cas d'erreur.
     */
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

    /**
     * Crée une nouvelle catégorie de commerce dans le système.
     *
     * @param dto Les informations de la catégorie à créer.
     * @return 201 Created avec la catégorie créée, ou 400 si les données sont invalides.
     */
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

    /**
     * Met à jour une catégorie existante.
     *
     * @param id  L'UUID de la catégorie à modifier.
     * @param dto Les nouvelles données à appliquer.
     * @return 200 OK avec la catégorie modifiée, 404 si introuvable, ou 400 en cas d'erreur.
     */
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

    /**
     * Supprime définitivement une catégorie du système.
     *
     * @param id L'UUID de la catégorie à supprimer.
     * @return 204 No Content si la suppression réussit, ou 400 en cas d'erreur.
     */
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

    /**
     * Recherche les catégories dont le nom correspond au terme fourni.
     *
     * @param nom Le terme de recherche sur le nom de la catégorie.
     * @return 200 OK avec la liste des catégories correspondantes, ou 400 en cas d'erreur.
     */
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
