package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.CategorieDTO;
import com.carrefourconnect.services.interfaces.CategorieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categorie", description = "Gestion des catégories de commerce")
public class CategorieController {

    private final CategorieService service;

    @GetMapping
    @Operation(summary = "Liste toutes les catégories")
    public List<CategorieDTO> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère une catégorie par son ID")
    public ResponseEntity<CategorieDTO> getById(@PathVariable UUID id) {
        CategorieDTO dto = service.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Crée une nouvelle catégorie")
    public ResponseEntity<CategorieDTO> create(@RequestBody CategorieDTO dto) {
        return new ResponseEntity<>(service.save(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour une catégorie")
    public ResponseEntity<CategorieDTO> update(@PathVariable UUID id, @RequestBody CategorieDTO dto) {
        CategorieDTO updated = service.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime une catégorie")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/nom/{nom}")
    @Operation(summary = "Recherche une catégorie par son nom")
    public ResponseEntity<CategorieDTO> getByNom(@PathVariable String nom) {
        CategorieDTO dto = service.findByNom(nom);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }
}
