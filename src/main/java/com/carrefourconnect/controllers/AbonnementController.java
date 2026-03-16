package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.AbonnementDTO;
import com.carrefourconnect.services.interfaces.AbonnementService;
import com.carrefourconnect.utils.enums.StatutAbonnement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/abonnements")
@RequiredArgsConstructor
@Tag(name = "Abonnement", description = "Gestion des abonnements")
public class AbonnementController {

    private final AbonnementService service;

    @GetMapping
    @Operation(summary = "Liste tous les abonnements")
    public List<AbonnementDTO> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère un abonnement par son ID")
    public ResponseEntity<AbonnementDTO> getById(@PathVariable UUID id) {
        AbonnementDTO dto = service.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Crée un nouvel abonnement")
    public ResponseEntity<AbonnementDTO> create(@RequestBody AbonnementDTO dto) {
        return new ResponseEntity<>(service.save(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour un abonnement")
    public ResponseEntity<AbonnementDTO> update(@PathVariable UUID id, @RequestBody AbonnementDTO dto) {
        AbonnementDTO updated = service.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un abonnement")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statut/{statut}")
    @Operation(summary = "Filtre les abonnements par statut")
    public List<AbonnementDTO> getByStatut(@PathVariable StatutAbonnement statut) {
        return service.findByStatut(statut);
    }
}
