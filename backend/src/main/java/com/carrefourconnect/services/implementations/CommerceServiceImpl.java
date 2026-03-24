package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.CommerceDTO;
import com.carrefourconnect.entities.Commerce;
import com.carrefourconnect.mappers.CommerceMapper;
import com.carrefourconnect.repositories.CommerceRepository;
import com.carrefourconnect.services.interfaces.CommerceService;
import com.carrefourconnect.utils.enums.StatutCommerce;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommerceServiceImpl implements CommerceService {

    private static final Logger log = LoggerFactory.getLogger(CommerceServiceImpl.class);

    private final CommerceRepository repository;
    private final CommerceMapper mapper;

    public CommerceServiceImpl(CommerceRepository repository, CommerceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public CommerceDTO findById(UUID id) {
        log.debug("Récupération commerce ID: {}", id);
        return repository.findById(id).map(mapper::toDto).orElse(null);
    }

    @Override
    public List<CommerceDTO> findAll() {
        log.debug("Récupération de tous les commerces");
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public CommerceDTO save(CommerceDTO dto) {
        log.info("Enregistrement d'un nouveau commerce: {}", dto.getNom());
        Commerce entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public CommerceDTO update(UUID id, CommerceDTO dto) {
        log.info("Mise à jour du commerce ID: {}", id);
        if (repository.existsById(id)) {
            Commerce entity = mapper.toEntity(dto);
            entity.setIdcommerce(id);
            return mapper.toDto(repository.save(entity));
        }
        log.error("Commerce non trouvé pour mise à jour: {}", id);
        return null;
    }

    @Override
    public void delete(UUID id) {
        log.info("Suppression du commerce ID: {}", id);
        repository.deleteById(id);
    }

    @Override
    public List<CommerceDTO> findByCategorie(UUID categorieId) {
        log.debug("Recherche par catégorie ID: {}", categorieId);
        return repository.findByCategorie_Idcategorie(categorieId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<CommerceDTO> findByCommercant(UUID commercantId) {
        log.debug("Recherche par commerçant ID: {}", commercantId);
        return repository.findByCommercant_Iduser(commercantId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<CommerceDTO> searchByName(String name) {
        log.debug("Recherche par nom contenant: {}", name);
        return repository.findByNomContainingIgnoreCase(name).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<CommerceDTO> findByStatut(StatutCommerce statut) {
        log.debug("Filtrage par statut: {}", statut);
        return repository.findByStatut(statut).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<CommerceDTO> findNearby(double latitude, double longitude, double distanceInKm) {
        log.info("Recherche géo: lat={}, lon={}, dist={}km", latitude, longitude, distanceInKm);
        return repository.findNearby(latitude, longitude, distanceInKm * 1000).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommerceDTO> rechercher(String nom, UUID idCategorie, String ville, StatutCommerce statut, Double lat, Double lon, Double rayonKm) {
        log.info("Recherche avancée: nom={}, ville={}, proximité={}km", nom, ville, rayonKm);
        
        List<Commerce> resultats;
        
        // Si la proximité est demandée, on commence par là (car c'est le filtre le plus restrictif)
        if (lat != null && lon != null && rayonKm != null) {
            resultats = repository.findNearby(lat, lon, rayonKm * 1000);
        } else {
            resultats = repository.findAll();
        }

        // Appliquer les autres filtres manuellement pour cette version (ou utiliser Specification plus tard)
        return resultats.stream()
                .filter(c -> nom == null || c.getNom().toLowerCase().contains(nom.toLowerCase()))
                .filter(c -> statut == null || c.getStatut().equals(statut))
                .filter(c -> idCategorie == null || (c.getCategorie() != null && c.getCategorie().getIdcategorie().equals(idCategorie)))
                // Note: La ville est dans Localisation, donc on filtre via les localisations du commerce
                // Pour simplifier ici, on accepte si au moins une localisation match
                /*.filter(c -> ville == null || repository.checkVille(c.getIdcommerce(), ville))*/
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
