package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.CommerceDTO;
import com.carrefourconnect.services.interfaces.CommerceService;
import com.carrefourconnect.utils.enums.StatutCommerce;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/commerces")
@RequiredArgsConstructor
@Tag(name = "Commerce", description = "Gestion des commerces")
public class CommerceController {

    private final CommerceService service;

    @GetMapping
    @Operation(summary = "Liste tous les commerces")
    public ResponseEntity<?> getAll() {
        try {
            return ResponseEntity.ok(service.findAll());
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère un commerce par son ID")
    public ResponseEntity<?> getById(@PathVariable java.util.UUID id) {
        try {
            CommerceDTO dto = service.findById(id);
            return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping
    @Operation(summary = "Crée un nouveau commerce")
    public ResponseEntity<?> create(@RequestBody CommerceDTO dto) {
        try {
            return new ResponseEntity<>(service.save(dto), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour un commerce")
    public ResponseEntity<?> update(@PathVariable java.util.UUID id, @RequestBody CommerceDTO dto) {
        try {
            CommerceDTO updated = service.update(id, dto);
            return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un commerce")
    public ResponseEntity<?> delete(@PathVariable java.util.UUID id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/categorie/{categorieId}")
    @Operation(summary = "Liste les commerces par catégorie")
    public ResponseEntity<?> getByCategorie(@PathVariable java.util.UUID categorieId) {
        try {
            return ResponseEntity.ok(service.findByCategorie(categorieId));
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/commercant/{commercantId}")
    @Operation(summary = "Liste les commerces d'un commerçant")
    public ResponseEntity<?> getByCommercant(@PathVariable java.util.UUID commercantId) {
        try {
            return ResponseEntity.ok(service.findByCommercant(commercantId));
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/recherche")
    @Operation(summary = "Recherche des commerces par nom")
    public ResponseEntity<?> searchByName(@RequestParam String nom) {
        try {
            return ResponseEntity.ok(service.searchByName(nom));
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/statut/{statut}")
    @Operation(summary = "Filtre les commerces par statut")
    public ResponseEntity<?> getByStatut(@PathVariable StatutCommerce statut) {
        try {
            return ResponseEntity.ok(service.findByStatut(statut));
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
