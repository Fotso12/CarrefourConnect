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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/commerces")
@RequiredArgsConstructor
@Tag(name = "Commerce", description = "Gestion des commerces")
public class CommerceController {

    private final CommerceService service;

    @GetMapping
    @Operation(summary = "Liste tous les commerces")
    public List<CommerceDTO> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère un commerce par son ID")
    public ResponseEntity<CommerceDTO> getById(@PathVariable UUID id) {
        CommerceDTO dto = service.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Crée un nouveau commerce")
    public ResponseEntity<CommerceDTO> create(@RequestBody CommerceDTO dto) {
        return new ResponseEntity<>(service.save(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour un commerce")
    public ResponseEntity<CommerceDTO> update(@PathVariable UUID id, @RequestBody CommerceDTO dto) {
        CommerceDTO updated = service.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un commerce")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categorie/{categorieId}")
    @Operation(summary = "Liste les commerces par catégorie")
    public List<CommerceDTO> getByCategorie(@PathVariable UUID categorieId) {
        return service.findByCategorie(categorieId);
    }

    @GetMapping("/commercant/{commercantId}")
    @Operation(summary = "Liste les commerces d'un commerçant")
    public List<CommerceDTO> getByCommercant(@PathVariable UUID commercantId) {
        return service.findByCommercant(commercantId);
    }

    @GetMapping("/recherche")
    @Operation(summary = "Recherche des commerces par nom")
    public List<CommerceDTO> searchByName(@RequestParam String nom) {
        return service.searchByName(nom);
    }

    @GetMapping("/statut/{statut}")
    @Operation(summary = "Filtre les commerces par statut")
    public List<CommerceDTO> getByStatut(@PathVariable StatutCommerce statut) {
        return service.findByStatut(statut);
    }
}
