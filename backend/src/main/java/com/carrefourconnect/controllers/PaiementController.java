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

/**
 * Contrôleur REST pour la gestion des paiements.
 * Permet d'enregistrer et de consulter les transactions liées aux abonnements des commerçants.
 */
@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
@Tag(name = "Paiement", description = "Gestion des transactions de paiement")
@Slf4j
public class PaiementController {

    private final PaiementService service;

    /**
     * Retourne la liste de tous les paiements enregistrés dans le système.
     *
     * @return 200 OK avec la liste des paiements, ou 400 en cas d'erreur.
     */
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

    /**
     * Récupère un paiement spécifique par son identifiant unique.
     *
     * @param id L'UUID du paiement à récupérer.
     * @return 200 OK avec le paiement, 404 si introuvable, ou 400 en cas d'erreur.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Récupère un paiement par son ID")
    public ResponseEntity<?> getById(@PathVariable("id") UUID id) {
        log.info("Requête pour récupérer le paiement ID: {}", id);
        try {
            PaiementDTO dto = service.findById(id);
            return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur récupération paiement {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Enregistre une nouvelle transaction de paiement pour un abonnement.
     *
     * @param dto Les informations du paiement (montant, référence, abonnement, etc.).
     * @return 201 Created avec le paiement enregistré, ou 400 si les données sont invalides.
     */
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

    /**
     * Met à jour les informations d'un paiement existant (ex. mise à jour du statut).
     *
     * @param id  L'UUID du paiement à modifier.
     * @param dto Les nouvelles données à appliquer.
     * @return 200 OK avec le paiement modifié, 404 si introuvable, ou 400 en cas d'erreur.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Met à jour un paiement")
    public ResponseEntity<?> update(@PathVariable("id") UUID id, @RequestBody PaiementDTO dto) {
        log.info("Mise à jour du paiement ID: {}", id);
        try {
            PaiementDTO updated = service.update(id, dto);
            return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur mise à jour paiement {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Supprime définitivement un paiement du système.
     *
     * @param id L'UUID du paiement à supprimer.
     * @return 204 No Content si la suppression réussit, ou 400 en cas d'erreur.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un paiement")
    public ResponseEntity<?> delete(@PathVariable("id") UUID id) {
        log.info("Suppression du paiement ID: {}", id);
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Erreur suppression paiement {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retourne tous les paiements liés à un abonnement spécifique.
     *
     * @param abonnementId L'UUID de l'abonnement dont on veut les paiements.
     * @return 200 OK avec la liste des paiements de l'abonnement, ou 400 en cas d'erreur.
     */
    @GetMapping("/abonnement/{abonnementId}")
    @Operation(summary = "Liste les paiements d'un abonnement")
    public ResponseEntity<?> getByAbonnement(@PathVariable("abonnementId") UUID abonnementId) {
        log.info("Récupération paiements pour l'abonnement: {}", abonnementId);
        try {
            return ResponseEntity.ok(service.findByAbonnement(abonnementId));
        } catch (Exception e) {
            log.error("Erreur paiements abonnement {}: {}", abonnementId, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Récupère un paiement par sa référence transactionnelle unique.
     *
     * @param reference La référence unique de la transaction.
     * @return 200 OK avec le paiement trouvé, 404 si introuvable, ou 400 en cas d'erreur.
     */
    @GetMapping("/reference/{reference}")
    @Operation(summary = "Récupère un paiement par sa référence")
    public ResponseEntity<?> getByReference(@PathVariable("reference") String reference) {
        log.info("Recherche paiement par référence: {}", reference);
        try {
            PaiementDTO dto = service.findByReference(reference);
            return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur recherche référence {}: {}", reference, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Filtre les paiements selon leur statut (EN_ATTENTE, VALIDE, ECHEC, etc.).
     *
     * @param statut Le statut à utiliser comme filtre.
     * @return 200 OK avec la liste des paiements filtrés, ou 400 en cas d'erreur.
     */
    @GetMapping("/statut/{statut}")
    @Operation(summary = "Filtre les paiements par statut")
    public ResponseEntity<?> getByStatut(@PathVariable("statut") StatutPaiement statut) {
        log.info("Filtrage des paiements par statut: {}", statut);
        try {
            return ResponseEntity.ok(service.findByStatut(statut));
        } catch (Exception e) {
            log.error("Erreur filtrage paiements statut {}: {}", statut, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
