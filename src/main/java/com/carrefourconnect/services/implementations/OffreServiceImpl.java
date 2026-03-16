package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.OffreDTO;
import com.carrefourconnect.entities.Offre;
import com.carrefourconnect.mappers.OffreMapper;
import com.carrefourconnect.repositories.OffreRepository;
import com.carrefourconnect.services.interfaces.OffreService;
import com.carrefourconnect.utils.enums.StatutOffre;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OffreServiceImpl implements OffreService {

    private final OffreRepository repository;
    private final OffreMapper mapper;

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
