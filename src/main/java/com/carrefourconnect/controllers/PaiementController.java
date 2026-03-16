package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.PaiementDTO;
import com.carrefourconnect.services.interfaces.PaiementService;
import com.carrefourconnect.utils.enums.StatutPaiement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
@Tag(name = "Paiement", description = "Gestion des transactions de paiement")
public class PaiementController {

    private final PaiementService service;

    @GetMapping
    @Operation(summary = "Liste tous les paiements")
    public ResponseEntity<?> getAll() {
        try {
            return ResponseEntity.ok(service.findAll());
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère un paiement par son ID")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        try {
            PaiementDTO dto = service.findById(id);
            return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping
    @Operation(summary = "Enregistre un nouveau paiement")
    public ResponseEntity<?> save(@RequestBody PaiementDTO dto) {
        try {
            return new ResponseEntity<>(service.save(dto), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour un paiement")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody PaiementDTO dto) {
        try {
            PaiementDTO updated = service.update(id, dto);
            return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un paiement")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/abonnement/{abonnementId}")
    @Operation(summary = "Liste les paiements d'un abonnement")
    public ResponseEntity<?> getByAbonnement(@PathVariable UUID abonnementId) {
        try {
            return ResponseEntity.ok(service.findByAbonnement(abonnementId));
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/reference/{reference}")
    @Operation(summary = "Récupère un paiement par sa référence")
    public ResponseEntity<?> getByReference(@PathVariable String reference) {
        try {
            PaiementDTO dto = service.findByReference(reference);
            return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/statut/{statut}")
    @Operation(summary = "Filtre les paiements par statut")
    public ResponseEntity<?> getByStatut(@PathVariable StatutPaiement statut) {
        try {
            return ResponseEntity.ok(service.findByStatut(statut));
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
