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

/**
 * Contrôleur REST pour la gestion des localisations.
 * Permet d'associer des adresses et coordonnées géographiques aux commerces.
 */
@RestController
@RequestMapping("/api/localisations")
@RequiredArgsConstructor
@Tag(name = "Localisation", description = "Gestion des emplacements géographiques")
@Slf4j
public class LocalisationController {

    private final LocalisationService service;

    /**
     * Retourne la liste de toutes les localisations enregistrées.
     *
     * @return 200 OK avec la liste des localisations, ou 400 en cas d'erreur.
     */
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

    /**
     * Récupère une localisation par son identifiant unique.
     *
     * @param id L'UUID de la localisation à récupérer.
     * @return 200 OK avec la localisation, 404 si introuvable, ou 400 en cas d'erreur.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Récupère une localisation par son ID")
    public ResponseEntity<?> getById(@PathVariable("id") UUID id) {
        log.info("Requête pour récupérer la localisation ID: {}", id);
        try {
            LocalisationDTO dto = service.findById(id);
            return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur récupération localisation {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Crée une nouvelle localisation et l'associe à un commerce.
     *
     * @param dto Les informations de la localisation à créer (adresse, coordonnées GPS, etc.).
     * @return 201 Created avec la localisation créée, ou 400 si les données sont invalides.
     */
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

    /**
     * Met à jour une localisation existante (ex. changement d'adresse d'un commerce).
     *
     * @param id  L'UUID de la localisation à modifier.
     * @param dto Les nouvelles données à appliquer.
     * @return 200 OK avec la localisation modifiée, 404 si introuvable, ou 400 en cas d'erreur.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Met à jour une localisation")
    public ResponseEntity<?> update(@PathVariable("id") UUID id, @RequestBody LocalisationDTO dto) {
        log.info("Mise à jour de la localisation ID: {}", id);
        try {
            LocalisationDTO updated = service.update(id, dto);
            return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur mise à jour localisation {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Supprime définitivement une localisation du système.
     *
     * @param id L'UUID de la localisation à supprimer.
     * @return 204 No Content si la suppression réussit, ou 400 en cas d'erreur.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime une localisation")
    public ResponseEntity<?> delete(@PathVariable("id") UUID id) {
        log.info("Suppression de la localisation ID: {}", id);
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Erreur suppression localisation {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retourne toutes les localisations associées à un commerce donné.
     *
     * @param commerceId L'UUID du commerce dont on veut les localisations.
     * @return 200 OK avec la liste des localisations du commerce, ou 400 en cas d'erreur.
     */
    @GetMapping("/commerce/{commerceId}")
    @Operation(summary = "Liste les localisations d'un commerce")
    public ResponseEntity<?> getByCommerce(@PathVariable("commerceId") UUID commerceId) {
        log.info("Récupération localisations commerce: {}", commerceId);
        try {
            return ResponseEntity.ok(service.findByCommerce(commerceId));
        } catch (Exception e) {
            log.error("Erreur localisations commerce {}: {}", commerceId, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retourne toutes les localisations situées dans une ville donnée.
     *
     * @param ville Le nom de la ville à utiliser comme filtre.
     * @return 200 OK avec la liste des localisations de la ville, ou 400 en cas d'erreur.
     */
    @GetMapping("/ville/{ville}")
    @Operation(summary = "Liste les localisations d'une ville")
    public ResponseEntity<?> getByVille(@PathVariable("ville") String ville) {
        log.info("Récupération localisations ville: {}", ville);
        try {
            return ResponseEntity.ok(service.findByVille(ville));
        } catch (Exception e) {
            log.error("Erreur localisations ville {}: {}", ville, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
