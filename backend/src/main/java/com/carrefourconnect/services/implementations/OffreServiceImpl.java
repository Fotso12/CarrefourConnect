package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.OffreDTO;
import com.carrefourconnect.entities.Offre;
import com.carrefourconnect.mappers.OffreMapper;
import com.carrefourconnect.repositories.OffreRepository;
import com.carrefourconnect.repositories.CommerceRepository;
import com.carrefourconnect.services.interfaces.OffreService;
import com.carrefourconnect.utils.enums.StatutOffre;
import com.carrefourconnect.utils.enums.TypeOffre;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OffreServiceImpl implements OffreService {

    private static final Logger log = LoggerFactory.getLogger(OffreServiceImpl.class);

    private final OffreRepository repository;
    private final CommerceRepository commerceRepository;
    private final OffreMapper mapper;

    public OffreServiceImpl(OffreRepository repository, CommerceRepository commerceRepository, OffreMapper mapper) {
        this.repository = repository;
        this.commerceRepository = commerceRepository;
        this.mapper = mapper;
    }

    @Override
    public OffreDTO findById(UUID id) {
        log.debug("Récupération offre ID: {}", id);
        return repository.findById(id).map(mapper::toDto).orElse(null);
    }

    @Override
    public List<OffreDTO> findAll() {
        log.debug("Récupération de toutes les offres");
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public OffreDTO save(OffreDTO dto) {
        log.info("Création d'une nouvelle offre: {}", dto.getTitre());
        Offre entity = mapper.toEntity(dto);
        if (dto.getIdcommerce() != null) {
            entity.setCommerce(commerceRepository.findById(dto.getIdcommerce()).orElseThrow(() -> new RuntimeException("Commerce non trouvé")));
        }
        // Valeurs par défaut pour les champs obligatoires non envoyés par le frontend
        if (entity.getStatut() == null) {
            entity.setStatut(StatutOffre.ACTIF);
        }
        if (entity.getType() == null) {
            entity.setType(TypeOffre.PROMOTION);
        }
        if (entity.getDateDebut() == null) {
            entity.setDateDebut(LocalDateTime.now());
        }
        if (entity.getDateFin() == null) {
            entity.setDateFin(LocalDateTime.now().plusDays(30));
        }
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public OffreDTO update(UUID id, OffreDTO dto) {
        log.info("Mise à jour de l'offre ID: {}", id);
        if (repository.existsById(id)) {
            Offre entity = mapper.toEntity(dto);
            entity.setIdoffre(id);
            return mapper.toDto(repository.save(entity));
        }
        log.error("Offre non trouvée pour mise à jour: {}", id);
        return null;
    }

    @Override
    public void delete(UUID id) {
        log.info("Suppression de l'offre ID: {}", id);
        repository.deleteById(id);
    }

    @Override
    public List<OffreDTO> findByCommerce(UUID commerceId) {
        log.debug("Récupération offres pour commerce ID: {}", commerceId);
        return repository.findByCommerce_Idcommerce(commerceId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<OffreDTO> findActiveOffres() {
        log.debug("Recherche des offres actives");
        return repository.findActiveOffres().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<OffreDTO> findByStatut(StatutOffre statut) {
        log.debug("Filtrage offres par statut: {}", statut);
        return repository.findByStatut(statut).stream().map(mapper::toDto).collect(Collectors.toList());
    }
}
