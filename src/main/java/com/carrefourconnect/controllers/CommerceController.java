package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.CommerceDTO;
import com.carrefourconnect.services.interfaces.CommerceService;
import com.carrefourconnect.utils.enums.StatutCommerce;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/commerces")
@RequiredArgsConstructor
@Tag(name = "Commerce", description = "Gestion des boutiques et établissements")
@Slf4j
public class CommerceController {

    private final CommerceService service;

    @GetMapping
    @Operation(summary = "Liste tous les commerces")
    public ResponseEntity<?> getAll() {
        log.info("Requête pour lister tous les commerces");
        try {
            return ResponseEntity.ok(service.findAll());
        } catch (Exception e) {
            log.error("Erreur lister commerces: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère un commerce par son ID")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        log.info("Requête pour récupérer le commerce ID: {}", id);
        try {
            CommerceDTO dto = service.findById(id);
            return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur récupération commerce {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping
    @Operation(summary = "Crée un nouveau commerce")
    public ResponseEntity<?> save(@RequestBody CommerceDTO dto) {
        log.info("Requête de création commerce: {}", dto.getNom());
        try {
            return new ResponseEntity<>(service.save(dto), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Erreur création commerce {}: {}", dto.getNom(), e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour un commerce")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody CommerceDTO dto) {
        log.info("Requête de mise à jour commerce ID: {}", id);
        try {
            CommerceDTO updated = service.update(id, dto);
            return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur mise à jour commerce {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un commerce")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        log.info("Requête de suppression commerce ID: {}", id);
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Erreur suppression commerce {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/categorie/{categorieId}")
    @Operation(summary = "Liste les commerces par catégorie")
    public ResponseEntity<?> getByCategorie(@PathVariable UUID categorieId) {
        log.info("Recherche commerces par catégorie: {}", categorieId);
        try {
            return ResponseEntity.ok(service.findByCategorie(categorieId));
        } catch (Exception e) {
            log.error("Erreur recherche par catégorie {}: {}", categorieId, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/commercant/{commercantId}")
    @Operation(summary = "Liste les commerces d'un commerçant")
    public ResponseEntity<?> getByCommercant(@PathVariable UUID commercantId) {
        log.info("Recherche commerces par commerçant: {}", commercantId);
        try {
            return ResponseEntity.ok(service.findByCommercant(commercantId));
        } catch (Exception e) {
            log.error("Erreur recherche par commerçant {}: {}", commercantId, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/recherche")
    @Operation(summary = "Recherche un commerce par nom")
    public ResponseEntity<?> searchByName(@RequestParam String nom) {
        log.info("Recherche commerce par nom: {}", nom);
        try {
            return ResponseEntity.ok(service.searchByName(nom));
        } catch (Exception e) {
            log.error("Erreur recherche par nom {}: {}", nom, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/statut/{statut}")
    @Operation(summary = "Filtre les commerces par statut")
    public ResponseEntity<?> getByStatut(@PathVariable StatutCommerce statut) {
        log.info("Filtrage commerces par statut: {}", statut);
        try {
            return ResponseEntity.ok(service.findByStatut(statut));
        } catch (Exception e) {
            log.error("Erreur filtrage statut {}: {}", statut, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/proximite")
    @Operation(summary = "Recherche des commerces par proximité (lat, lon, distance en km)")
    public ResponseEntity<?> findNearby(@RequestParam double lat, @RequestParam double lon, @RequestParam double distance) {
        log.info("Recherche de proximité: lat={}, lon={}, dist={}km", lat, lon, distance);
        try {
            return ResponseEntity.ok(service.findNearby(lat, lon, distance));
        } catch (Exception e) {
            log.error("Erreur recherche proximité: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
