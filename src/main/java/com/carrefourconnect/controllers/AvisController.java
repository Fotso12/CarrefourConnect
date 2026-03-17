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

/**
 * Contrôleur REST pour la gestion des avis.
 * Permet aux visiteurs de soumettre des avis sur les commerces et à l'admin de les modérer.
 */
@RestController
@RequestMapping("/api/avis")
@RequiredArgsConstructor
@Tag(name = "Avis", description = "Gestion des avis et notes des visiteurs")
@Slf4j
public class AvisController {

    private final AvisService service;

    /**
     * Retourne la liste de tous les avis enregistrés dans le système.
     *
     * @return 200 OK avec la liste des avis, ou 400 en cas d'erreur.
     */
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

    /**
     * Récupère un avis spécifique par son identifiant unique.
     *
     * @param id L'UUID de l'avis à récupérer.
     * @return 200 OK avec l'avis, 404 si introuvable, ou 400 en cas d'erreur.
     */
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

    /**
     * Enregistre un nouvel avis d'un visiteur pour un commerce.
     *
     * @param dto Les informations de l'avis à enregistrer (note, commentaire, etc.).
     * @return 201 Created avec l'avis enregistré, ou 400 si les données sont invalides.
     */
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

    /**
     * Met à jour un avis existant (ex. lors de la modération par un administrateur).
     *
     * @param id  L'UUID de l'avis à modifier.
     * @param dto Les nouvelles données à appliquer.
     * @return 200 OK avec l'avis modifié, 404 si introuvable, ou 400 en cas d'erreur.
     */
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

    /**
     * Supprime définitivement un avis du système.
     *
     * @param id L'UUID de l'avis à supprimer.
     * @return 204 No Content si la suppression réussit, ou 400 en cas d'erreur.
     */
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

    /**
     * Retourne tous les avis associés à un commerce donné.
     *
     * @param commerceId L'UUID du commerce dont on veut consulter les avis.
     * @return 200 OK avec la liste des avis du commerce, ou 400 en cas d'erreur.
     */
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

    /**
     * Retourne tous les avis rédigés par un visiteur donné.
     *
     * @param visiteurId L'UUID du visiteur dont on veut consulter les avis.
     * @return 200 OK avec la liste des avis du visiteur, ou 400 en cas d'erreur.
     */
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

    /**
     * Filtre les avis selon leur statut de modération (EN_ATTENTE, APPROUVE, REJETE).
     *
     * @param status Le statut à utiliser comme filtre.
     * @return 200 OK avec la liste des avis filtrés, ou 400 en cas d'erreur.
     */
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
