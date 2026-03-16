package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.LocalisationDTO;
import com.carrefourconnect.services.interfaces.LocalisationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/localisations")
@RequiredArgsConstructor
@Tag(name = "Localisation", description = "Gestion des données de localisation des commerces")
public class LocalisationController {

    private final LocalisationService service;

    @GetMapping
    @Operation(summary = "Liste toutes les localisations")
    public List<LocalisationDTO> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère une localisation par son ID")
    public ResponseEntity<LocalisationDTO> getById(@PathVariable UUID id) {
        LocalisationDTO dto = service.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Ajoute une nouvelle localisation")
    public ResponseEntity<LocalisationDTO> create(@RequestBody LocalisationDTO dto) {
        return new ResponseEntity<>(service.save(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour une localisation")
    public ResponseEntity<LocalisationDTO> update(@PathVariable UUID id, @RequestBody LocalisationDTO dto) {
        LocalisationDTO updated = service.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime une localisation")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/commerce/{commerceId}")
    @Operation(summary = "Liste les localisations d'un commerce")
    public List<LocalisationDTO> getByCommerce(@PathVariable UUID commerceId) {
        return service.findByCommerce(commerceId);
    }

    @GetMapping("/ville/{ville}")
    @Operation(summary = "Liste les commerces dans une ville")
    public List<LocalisationDTO> getByVille(@PathVariable String ville) {
        return service.findByVille(ville);
    }
}
