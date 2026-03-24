package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.OffreDTO;
import com.carrefourconnect.services.interfaces.OffreService;
import com.carrefourconnect.utils.enums.StatutOffre;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Contrôleur REST pour la gestion des offres et promotions.
 * Permet aux commerçants de publier des offres spéciales associées à leurs commerces.
 */
@RestController
@RequestMapping("/api/offres")
@RequiredArgsConstructor
@Tag(name = "Offre", description = "Gestion des promotions et offres spéciales")
@Slf4j
public class OffreController {

    private final OffreService service;

    /**
     * Retourne la liste de toutes les offres du système.
     *
     * @return 200 OK avec la liste des offres, ou 400 en cas d'erreur.
     */
    @GetMapping
    @Operation(summary = "Liste toutes les offres")
    public ResponseEntity<?> getAll() {
        log.info("Requête pour lister toutes les offres");
        try {
            return ResponseEntity.ok(service.findAll());
        } catch (Exception e) {
            log.error("Erreur lister offres: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Récupère une offre spécifique par son identifiant unique.
     *
     * @param id L'UUID de l'offre à récupérer.
     * @return 200 OK avec l'offre, 404 si introuvable, ou 400 en cas d'erreur.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Récupère une offre par son ID")
    public ResponseEntity<?> getById(@PathVariable("id") UUID id) {
        log.info("Requête pour récupérer l'offre ID: {}", id);
        try {
            OffreDTO dto = service.findById(id);
            return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur récupération offre {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Crée une nouvelle offre ou promotion pour un commerce.
     *
     * @param dto Les informations de l'offre à créer (titre, description, dates, etc.).
     * @return 201 Created avec l'offre créée, ou 400 si les données sont invalides.
     */
    @PostMapping
    @Operation(summary = "Crée une nouvelle offre")
    public ResponseEntity<?> save(@RequestBody OffreDTO dto) {
        log.info("Requête de création d'offre: {}", dto.getTitre());
        try {
            return new ResponseEntity<>(service.save(dto), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Erreur création offre {}: {}", dto.getTitre(), e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Met à jour les informations d'une offre existante.
     *
     * @param id  L'UUID de l'offre à modifier.
     * @param dto Les nouvelles données à appliquer.
     * @return 200 OK avec l'offre modifiée, 404 si introuvable, ou 400 en cas d'erreur.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Met à jour une offre")
    public ResponseEntity<?> update(@PathVariable("id") UUID id, @RequestBody OffreDTO dto) {
        log.info("Requête de mise à jour d'offre ID: {}", id);
        try {
            OffreDTO updated = service.update(id, dto);
            return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur mise à jour offre {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Supprime définitivement une offre du système.
     *
     * @param id L'UUID de l'offre à supprimer.
     * @return 204 No Content si la suppression réussit, ou 400 en cas d'erreur.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime une offre")
    public ResponseEntity<?> delete(@PathVariable("id") UUID id) {
        log.info("Requête de suppression d'offre ID: {}", id);
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Erreur suppression offre {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retourne toutes les offres publiées par un commerce donné.
     *
     * @param commerceId L'UUID du commerce dont on veut les offres.
     * @return 200 OK avec la liste des offres du commerce, ou 400 en cas d'erreur.
     */
    @GetMapping("/commerce/{commerceId}")
    @Operation(summary = "Liste les offres d'un commerce")
    public ResponseEntity<?> getByCommerce(@PathVariable("commerceId") UUID commerceId) {
        log.info("Récupération des offres pour le commerce: {}", commerceId);
        try {
            return ResponseEntity.ok(service.findByCommerce(commerceId));
        } catch (Exception e) {
            log.error("Erreur offres commerce {}: {}", commerceId, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retourne toutes les offres actuellement actives (non expirées).
     *
     * @return 200 OK avec la liste des offres actives, ou 400 en cas d'erreur.
     */
    @GetMapping("/active")
    @Operation(summary = "Liste toutes les offres actives")
    public ResponseEntity<?> getActiveOffres() {
        log.info("Récupération de toutes les offres actives");
        try {
            return ResponseEntity.ok(service.findActiveOffres());
        } catch (Exception e) {
            log.error("Erreur offres actives: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Filtre les offres selon leur statut (ACTIVE, EXPIREE, EN_ATTENTE).
     *
     * @param statut Le statut à utiliser comme filtre.
     * @return 200 OK avec la liste des offres filtrées, ou 400 en cas d'erreur.
     */
    @GetMapping("/statut/{statut}")
    @Operation(summary = "Filtre les offres par statut")
    public ResponseEntity<?> getByStatut(@PathVariable("statut") StatutOffre statut) {
        log.info("Filtrage des offres par statut: {}", statut);
        try {
            return ResponseEntity.ok(service.findByStatut(statut));
        } catch (Exception e) {
            log.error("Erreur filtrage offres statut {}: {}", statut, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
