package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.CommercantDTO;
import com.carrefourconnect.dtos.UtilisateurDTO;
import com.carrefourconnect.dtos.VisiteurDTO;
import com.carrefourconnect.services.interfaces.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion des utilisateurs.
 * Expose les endpoints pour l'inscription, la consultation et la gestion des favoris.
 */
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@Tag(name = "Utilisateur", description = "Gestion des utilisateurs et inscriptions")
@Slf4j
public class UtilisateurController {

    private final UtilisateurService service;

    /**
     * Retourne la liste complète de tous les utilisateurs enregistrés.
     *
     * @return 200 OK avec la liste des utilisateurs, ou 400 en cas d'erreur.
     */
    @GetMapping
    @Operation(summary = "Liste tous les utilisateurs")
    public ResponseEntity<?> getAll() {
        log.info("Requête pour lister tous les utilisateurs");
        try {
            return ResponseEntity.ok(service.findAll());
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des utilisateurs: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Récupère un utilisateur spécifique par son identifiant unique.
     *
     * @param id L'UUID de l'utilisateur à récupérer.
     * @return 200 OK avec l'utilisateur, 404 si introuvable, ou 400 en cas d'erreur.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Récupère un utilisateur par son ID")
    public ResponseEntity<?> getById(@PathVariable("id") java.util.UUID id) {
        log.info("Requête pour récupérer l'utilisateur ID: {}", id);
        try {
            UtilisateurDTO dto = service.findById(id);
            return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'utilisateur {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Inscrit un nouveau visiteur dans le système.
     *
     * @param dto Les informations du visiteur à inscrire.
     * @return 201 Created avec le visiteur créé, ou 400 si les données sont invalides.
     */
    @PostMapping("/inscription/visiteur")
    @Operation(summary = "Inscrit un nouveau visiteur")
    public ResponseEntity<?> registerVisiteur(@RequestBody VisiteurDTO dto) {
        log.info("Requête d'inscription pour un nouveau visiteur: {}", dto.getEmail());
        try {
            return new ResponseEntity<>(service.registerVisiteur(dto), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Erreur lors de l'inscription du visiteur {}: {}", dto.getEmail(), e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Inscrit un nouveau commerçant dans le système.
     *
     * @param dto Les informations du commerçant à inscrire.
     * @return 201 Created avec le commerçant créé, ou 400 si les données sont invalides.
     */
    @PostMapping("/inscription/commercant")
    @Operation(summary = "Inscrit un nouveau commerçant")
    public ResponseEntity<?> registerCommercant(@RequestBody CommercantDTO dto) {
        log.info("Requête d'inscription pour un nouveau commerçant: {}", dto.getEmail());
        try {
            return new ResponseEntity<>(service.registerCommercant(dto), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Erreur lors de l'inscription du commerçant {}: {}", dto.getEmail(), e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Met à jour les informations d'un utilisateur existant.
     *
     * @param id  L'UUID de l'utilisateur à modifier.
     * @param dto Les nouvelles données à appliquer.
     * @return 200 OK avec l'utilisateur modifié, 404 si introuvable, ou 400 en cas d'erreur.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Met à jour un utilisateur")
    public ResponseEntity<?> update(@PathVariable("id") java.util.UUID id, @RequestBody UtilisateurDTO dto) {
        log.info("Requête de mise à jour pour l'utilisateur ID: {}", id);
        try {
            UtilisateurDTO updated = service.update(id, dto);
            return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de l'utilisateur {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    static class ChangePasswordRequest {
        public String ancienMotDePasse;
        public String nouveauMotDePasse;
    }

    @PutMapping("/{id}/mot-de-passe")
    @Operation(summary = "Change le mot de passe d'un utilisateur en vérifiant l'ancien mot de passe")
    public ResponseEntity<?> changePassword(@PathVariable("id") java.util.UUID id, @RequestBody ChangePasswordRequest req) {
        log.info("Requête changement mot de passe pour utilisateur ID: {}", id);
        try {
            boolean ok = service.changePassword(id, req.ancienMotDePasse == null ? "" : req.ancienMotDePasse, req.nouveauMotDePasse == null ? "" : req.nouveauMotDePasse);
            if (!ok) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ancien mot de passe invalide ou utilisateur introuvable");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erreur changement mot de passe {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Supprime définitivement un utilisateur du système.
     *
     * @param id L'UUID de l'utilisateur à supprimer.
     * @return 204 No Content si la suppression réussit, ou 400 en cas d'erreur.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un utilisateur")
    public ResponseEntity<?> delete(@PathVariable("id") java.util.UUID id) {
        log.info("Requête de suppression pour l'utilisateur ID: {}", id);
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'utilisateur {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Recherche un utilisateur par son adresse email.
     *
     * @param email L'email de l'utilisateur à rechercher.
     * @return 200 OK avec l'utilisateur trouvé, 404 si aucun résultat, ou 400 en cas d'erreur.
     */
    @GetMapping("/email/{email}")
    @Operation(summary = "Recherche un utilisateur par email")
    public ResponseEntity<?> getByEmail(@PathVariable("email") String email) {
        log.info("Requête de recherche par email: {}", email);
        try {
            UtilisateurDTO dto = service.findByEmail(email);
            return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur lors de la recherche de l'utilisateur par email {}: {}", email, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Ajoute un commerce à la liste des favoris d'un utilisateur.
     *
     * @param userId     L'UUID de l'utilisateur.
     * @param commerceId L'UUID du commerce à ajouter en favori.
     * @return 200 OK si l'ajout réussit, ou 400 en cas d'erreur.
     */
    @PostMapping("/{userId}/favoris/{commerceId}")
    @Operation(summary = "Ajoute un commerce aux favoris de l'utilisateur")
    public ResponseEntity<?> addFavorite(@PathVariable("userId") java.util.UUID userId, @PathVariable("commerceId") java.util.UUID commerceId) {
        log.info("Ajout du commerce {} aux favoris de l'utilisateur {}", commerceId, userId);
        try {
            service.addFavorite(userId, commerceId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erreur lors de l'ajout du favori pour l'utilisateur {}: {}", userId, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retire un commerce de la liste des favoris d'un utilisateur.
     *
     * @param userId     L'UUID de l'utilisateur.
     * @param commerceId L'UUID du commerce à retirer des favoris.
     * @return 200 OK si le retrait réussit, ou 400 en cas d'erreur.
     */
    @DeleteMapping("/{userId}/favoris/{commerceId}")
    @Operation(summary = "Retire un commerce des favoris de l'utilisateur")
    public ResponseEntity<?> removeFavorite(@PathVariable("userId") java.util.UUID userId, @PathVariable("commerceId") java.util.UUID commerceId) {
        log.info("Retrait du commerce {} des favoris de l'utilisateur {}", commerceId, userId);
        try {
            service.removeFavorite(userId, commerceId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erreur lors du retrait du favori pour l'utilisateur {}: {}", userId, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retourne la liste des commerces favoris d'un utilisateur.
     *
     * @param userId L'UUID de l'utilisateur dont on veut les favoris.
     * @return 200 OK avec la liste des IDs de commerces favoris, ou 400 en cas d'erreur.
     */
    @GetMapping("/{userId}/favoris")
    @Operation(summary = "Liste les IDs des commerces favoris d'un utilisateur")
    public ResponseEntity<?> getFavorites(@PathVariable("userId") java.util.UUID userId) {
        log.info("Récupération des favoris pour l'utilisateur ID: {}", userId);
        try {
            return ResponseEntity.ok(service.getFavorites(userId));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des favoris pour l'utilisateur {}: {}", userId, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/non-admins")
    @Operation(summary = "Liste tous les utilisateurs sauf les administrateurs")
    public ResponseEntity<?> getAllNonAdmins() {
        log.info("Requête pour lister les utilisateurs non-admins");
        try {
            return ResponseEntity.ok(service.findAllNonAdmins());
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des utilisateurs: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}/suspendre")
    @Operation(summary = "Suspend un utilisateur avec un motif")
    public ResponseEntity<?> suspendre(@PathVariable("id") java.util.UUID id, @RequestParam("motif") String motif) {
        log.info("Requête de suspension utilisateur ID: {}", id);
        try {
            service.suspendre(id, motif);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erreur suspension utilisateur {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}/activer")
    @Operation(summary = "Active un utilisateur")
    public ResponseEntity<?> activer(@PathVariable("id") java.util.UUID id) {
        log.info("Requête d'activation utilisateur ID: {}", id);
        try {
            service.activer(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erreur activation utilisateur {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
