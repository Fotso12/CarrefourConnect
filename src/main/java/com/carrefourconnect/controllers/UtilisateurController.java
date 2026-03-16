package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.CommercantDTO;
import com.carrefourconnect.dtos.UtilisateurDTO;
import com.carrefourconnect.dtos.VisiteurDTO;
import com.carrefourconnect.services.interfaces.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@Tag(name = "Utilisateur", description = "Gestion des utilisateurs et inscriptions")
public class UtilisateurController {

    private final UtilisateurService service;

    @GetMapping
    @Operation(summary = "Liste tous les utilisateurs")
    public List<UtilisateurDTO> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère un utilisateur par son ID")
    public ResponseEntity<UtilisateurDTO> getById(@PathVariable UUID id) {
        UtilisateurDTO dto = service.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping("/inscription/visiteur")
    @Operation(summary = "Inscrit un nouveau visiteur")
    public ResponseEntity<UtilisateurDTO> registerVisiteur(@RequestBody VisiteurDTO dto) {
        return new ResponseEntity<>(service.registerVisiteur(dto), HttpStatus.CREATED);
    }

    @PostMapping("/inscription/commercant")
    @Operation(summary = "Inscrit un nouveau commerçant")
    public ResponseEntity<UtilisateurDTO> registerCommercant(@RequestBody CommercantDTO dto) {
        return new ResponseEntity<>(service.registerCommercant(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour un utilisateur")
    public ResponseEntity<UtilisateurDTO> update(@PathVariable UUID id, @RequestBody UtilisateurDTO dto) {
        UtilisateurDTO updated = service.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un utilisateur")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Recherche un utilisateur par email")
    public ResponseEntity<UtilisateurDTO> getByEmail(@PathVariable String email) {
        UtilisateurDTO dto = service.findByEmail(email);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }
}
