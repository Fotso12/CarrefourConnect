package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.AvisDTO;
import com.carrefourconnect.services.interfaces.AvisService;
import com.carrefourconnect.utils.enums.StatutAvis;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/avis")
@RequiredArgsConstructor
@Tag(name = "Avis", description = "Gestion des avis et notations")
public class AvisController {

    private final AvisService service;

    @GetMapping
    @Operation(summary = "Liste tous les avis")
    public List<AvisDTO> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère un avis par son ID")
    public ResponseEntity<AvisDTO> getById(@PathVariable UUID id) {
        AvisDTO dto = service.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Publie un nouvel avis")
    public ResponseEntity<AvisDTO> create(@RequestBody AvisDTO dto) {
        return new ResponseEntity<>(service.save(dto), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un avis")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/commerce/{commerceId}")
    @Operation(summary = "Liste les avis d'un commerce")
    public List<AvisDTO> getByCommerce(@PathVariable UUID commerceId) {
        return service.findByCommerce(commerceId);
    }

    @GetMapping("/visiteur/{visiteurId}")
    @Operation(summary = "Liste les avis publiés par un visiteur")
    public List<AvisDTO> getByVisiteur(@PathVariable UUID visiteurId) {
        return service.findByVisiteur(visiteurId);
    }

    @GetMapping("/statut/{statut}")
    @Operation(summary = "Filtre les avis par statut")
    public List<AvisDTO> getByStatut(@PathVariable StatutAvis statut) {
        return service.findByStatus(statut);
    }
}
