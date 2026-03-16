package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.OffreDTO;
import com.carrefourconnect.services.interfaces.OffreService;
import com.carrefourconnect.utils.enums.StatutOffre;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/offres")
@RequiredArgsConstructor
@Tag(name = "Offre", description = "Gestion des offres")
public class OffreController {

    private final OffreService service;

    @GetMapping
    @Operation(summary = "Liste toutes les offres")
    public List<OffreDTO> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère une offre par son ID")
    public ResponseEntity<OffreDTO> getById(@PathVariable UUID id) {
        OffreDTO dto = service.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Crée une nouvelle offre")
    public ResponseEntity<OffreDTO> create(@RequestBody OffreDTO dto) {
        return new ResponseEntity<>(service.save(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour une offre")
    public ResponseEntity<OffreDTO> update(@PathVariable UUID id, @RequestBody OffreDTO dto) {
        OffreDTO updated = service.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime une offre")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/commerce/{commerceId}")
    @Operation(summary = "Liste les offres d'un commerce")
    public List<OffreDTO> getByCommerce(@PathVariable UUID commerceId) {
        return service.findByCommerce(commerceId);
    }

    @GetMapping("/actives")
    @Operation(summary = "Liste les offres en cours de validité")
    public List<OffreDTO> getActiveOffres() {
        return service.findActiveOffres();
    }

    @GetMapping("/statut/{statut}")
    @Operation(summary = "Filtre les offres par statut")
    public List<OffreDTO> getByStatut(@PathVariable StatutOffre statut) {
        return service.findByStatut(statut);
    }
}
