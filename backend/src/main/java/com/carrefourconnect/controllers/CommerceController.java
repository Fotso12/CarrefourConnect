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
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

/**
 * Contrôleur REST pour la gestion des commerces.
 * Expose les endpoints pour créer, consulter, rechercher et filtrer les établissements.
 */
@RestController
@RequestMapping("/api/commerces")
@RequiredArgsConstructor
@Tag(name = "Commerce", description = "Gestion des boutiques et établissements")
@Slf4j
public class CommerceController {

    private final CommerceService service;

    /**
     * Retourne la liste complète de tous les commerces enregistrés.
     *
     * @return 200 OK avec la liste des commerces, ou 400 en cas d'erreur.
     */
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

    /**
     * Récupère un commerce spécifique par son identifiant unique.
     *
     * @param id L'UUID du commerce à récupérer.
     * @return 200 OK avec le commerce, 404 si introuvable, ou 400 en cas d'erreur.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Récupère un commerce par son ID")
    public ResponseEntity<?> getById(@PathVariable("id") UUID id) {
        log.info("Requête pour récupérer le commerce ID: {}", id);
        try {
            CommerceDTO dto = service.findById(id);
            return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur récupération commerce {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Crée un nouveau commerce dans le système.
     *
     * @param dto Les informations du commerce à créer.
     * @return 201 Created avec le commerce créé, ou 400 si les données sont invalides.
     */
    @PostMapping
    @Operation(summary = "Crée un nouveau commerce")
    public ResponseEntity<?> save(@RequestBody CommerceDTO dto, HttpServletRequest request) {
        log.info("Requête de création commerce: {}", dto.getNom());
        // Log temporaire pour debug : afficher l'en-tête Authorization reçu
        try {
            String authHeader = request.getHeader("Authorization");
            log.debug("Authorization header reçu: {}", authHeader);
        } catch (Exception e) {
            log.warn("Impossible de lire l'en-tête Authorization: {}", e.getMessage());
        }
        try {
            return new ResponseEntity<>(service.save(dto), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Erreur création commerce {}: {}", dto.getNom(), e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Met à jour les informations d'un commerce existant.
     *
     * @param id  L'UUID du commerce à modifier.
     * @param dto Les nouvelles données à appliquer.
     * @return 200 OK avec le commerce modifié, 404 si introuvable, ou 400 en cas d'erreur.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Met à jour un commerce")
    public ResponseEntity<?> update(@PathVariable("id") UUID id, @RequestBody CommerceDTO dto) {
        log.info("Requête de mise à jour commerce ID: {}", id);
        try {
            CommerceDTO updated = service.update(id, dto);
            return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur mise à jour commerce {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Supprime définitivement un commerce du système.
     *
     * @param id L'UUID du commerce à supprimer.
     * @return 204 No Content si la suppression réussit, ou 400 en cas d'erreur.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un commerce")
    public ResponseEntity<?> delete(@PathVariable("id") UUID id) {
        log.info("Requête de suppression commerce ID: {}", id);
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Erreur suppression commerce {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retourne tous les commerces appartenant à une catégorie donnée.
     *
     * @param categorieId L'UUID de la catégorie à filtrer.
     * @return 200 OK avec la liste des commerces de la catégorie, ou 400 en cas d'erreur.
     */
    @GetMapping("/categorie/{categorieId}")
    @Operation(summary = "Liste les commerces par catégorie")
    public ResponseEntity<?> getByCategorie(@PathVariable("categorieId") UUID categorieId) {
        log.info("Recherche commerces par catégorie: {}", categorieId);
        try {
            return ResponseEntity.ok(service.findByCategorie(categorieId));
        } catch (Exception e) {
            log.error("Erreur recherche par catégorie {}: {}", categorieId, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retourne tous les commerces gérés par un commerçant spécifique.
     *
     * @param commercantId L'UUID du commerçant propriétaire.
     * @return 200 OK avec la liste des commerces du commerçant, ou 400 en cas d'erreur.
     */
    @GetMapping("/commercant/{commercantId}")
    @Operation(summary = "Liste les commerces d'un commerçant")
    public ResponseEntity<?> getByCommercant(@PathVariable("commercantId") UUID commercantId) {
        log.info("Recherche commerces par commerçant: {}", commercantId);
        try {
            return ResponseEntity.ok(service.findByCommercant(commercantId));
        } catch (Exception e) {
            log.error("Erreur recherche par commerçant {}: {}", commercantId, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Recherche les commerces dont le nom contient le terme fourni.
     *
     * @param nom Le terme de recherche sur le nom du commerce.
     * @return 200 OK avec la liste des commerces correspondants, ou 400 en cas d'erreur.
     */
    @GetMapping("/recherche")
    @Operation(summary = "Recherche un commerce par nom")
    public ResponseEntity<?> searchByName(@RequestParam("nom") String nom) {
        log.info("Recherche commerce par nom: {}", nom);
        try {
            return ResponseEntity.ok(service.searchByName(nom));
        } catch (Exception e) {
            log.error("Erreur recherche par nom {}: {}", nom, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Filtre les commerces selon leur statut (ACTIF, EN_ATTENTE, FERME, etc.).
     *
     * @param statut Le statut à utiliser comme filtre.
     * @return 200 OK avec la liste des commerces filtrés, ou 400 en cas d'erreur.
     */
    @GetMapping("/statut/{statut}")
    @Operation(summary = "Filtre les commerces par statut")
    public ResponseEntity<?> getByStatut(@PathVariable("statut") StatutCommerce statut) {
        log.info("Filtrage commerces par statut: {}", statut);
        try {
            return ResponseEntity.ok(service.findByStatut(statut));
        } catch (Exception e) {
            log.error("Erreur filtrage statut {}: {}", statut, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Recherche les commerces situés dans un rayon donné autour d'un point géographique.
     *
     * @param lat      La latitude du point de référence.
     * @param lon      La longitude du point de référence.
     * @param distance Le rayon de recherche en kilomètres.
     * @return 200 OK avec la liste des commerces proches, ou 400 en cas d'erreur.
     */
    @GetMapping("/proximite")
    @Operation(summary = "Recherche des commerces par proximité (lat, lon, distance en km)")
    public ResponseEntity<?> findNearby(@RequestParam("lat") double lat, @RequestParam("lon") double lon, @RequestParam("distance") double distance) {
        log.info("Recherche de proximité: lat={}, lon={}, dist={}km", lat, lon, distance);
        try {
            return ResponseEntity.ok(service.findNearby(lat, lon, distance));
        } catch (Exception e) {
            log.error("Erreur recherche proximité: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Effectue une recherche multicritères (nom, catégorie, ville, proximité).
     */
    @GetMapping("/rechercher")
    @Operation(summary = "Recherche multicritères de commerces")
    public ResponseEntity<?> rechercher(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) UUID idCategorie,
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) StatutCommerce statut,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) Double rayon) {
        log.info("Requête de recherche multicritères");
        try {
            return ResponseEntity.ok(service.rechercher(nom, idCategorie, ville, statut, lat, lon, rayon));
        } catch (Exception e) {
            log.error("Erreur lors de la recherche: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}/suspendre")
    @Operation(summary = "Suspend un commerce avec un motif")
    public ResponseEntity<?> suspendre(@PathVariable("id") UUID id, @RequestParam("motif") String motif) {
        log.info("Requête de suspension commerce ID: {}", id);
        try {
            service.suspendre(id, motif);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erreur suspension commerce {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}/valider")
    @Operation(summary = "Valide un commerce")
    public ResponseEntity<?> valider(@PathVariable("id") UUID id) {
        log.info("Requête de validation commerce ID: {}", id);
        try {
            service.valider(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erreur validation commerce {}: {}", id, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
