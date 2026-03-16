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

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@Tag(name = "Utilisateur", description = "Gestion des utilisateurs et inscriptions")
@Slf4j
public class UtilisateurController {

    private final UtilisateurService service;

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

    @GetMapping("/{id}")
    @Operation(summary = "Récupère un utilisateur par son ID")
    public ResponseEntity<?> getById(@PathVariable java.util.UUID id) {
        log.info("Requête pour récupérer l'utilisateur ID: {}", id);
        try {
            UtilisateurDTO dto = service.findById(id);
            return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'utilisateur {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

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

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour un utilisateur")
    public ResponseEntity<?> update(@PathVariable java.util.UUID id, @RequestBody UtilisateurDTO dto) {
        log.info("Requête de mise à jour pour l'utilisateur ID: {}", id);
        try {
            UtilisateurDTO updated = service.update(id, dto);
            return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de l'utilisateur {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un utilisateur")
    public ResponseEntity<?> delete(@PathVariable java.util.UUID id) {
        log.info("Requête de suppression pour l'utilisateur ID: {}", id);
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'utilisateur {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Recherche un utilisateur par email")
    public ResponseEntity<?> getByEmail(@PathVariable String email) {
        log.info("Requête de recherche par email: {}", email);
        try {
            UtilisateurDTO dto = service.findByEmail(email);
            return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur lors de la recherche de l'utilisateur par email {}: {}", email, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{userId}/favoris/{commerceId}")
    @Operation(summary = "Ajoute un commerce aux favoris de l'utilisateur")
    public ResponseEntity<?> addFavorite(@PathVariable java.util.UUID userId, @PathVariable java.util.UUID commerceId) {
        log.info("Ajout du commerce {} aux favoris de l'utilisateur {}", commerceId, userId);
        try {
            service.addFavorite(userId, commerceId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erreur lors de l'ajout du favori pour l'utilisateur {}: {}", userId, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{userId}/favoris/{commerceId}")
    @Operation(summary = "Retire un commerce des favoris de l'utilisateur")
    public ResponseEntity<?> removeFavorite(@PathVariable java.util.UUID userId, @PathVariable java.util.UUID commerceId) {
        log.info("Retrait du commerce {} des favoris de l'utilisateur {}", commerceId, userId);
        try {
            service.removeFavorite(userId, commerceId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erreur lors du retrait du favori pour l'utilisateur {}: {}", userId, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{userId}/favoris")
    @Operation(summary = "Liste les IDs des commerces favoris d'un utilisateur")
    public ResponseEntity<?> getFavorites(@PathVariable java.util.UUID userId) {
        log.info("Récupération des favoris pour l'utilisateur ID: {}", userId);
        try {
            return ResponseEntity.ok(service.getFavorites(userId));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des favoris pour l'utilisateur {}: {}", userId, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
