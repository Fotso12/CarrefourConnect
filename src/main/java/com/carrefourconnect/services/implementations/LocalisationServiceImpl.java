package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.LocalisationDTO;
import com.carrefourconnect.entities.Localisation;
import com.carrefourconnect.mappers.LocalisationMapper;
import com.carrefourconnect.repositories.LocalisationRepository;
import com.carrefourconnect.services.interfaces.LocalisationService;
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
public class LocalisationServiceImpl implements LocalisationService {

    private final LocalisationRepository repository;
    private final LocalisationMapper mapper;

    @Override
    public LocalisationDTO findById(UUID id) {
        log.debug("Récupération localisation ID: {}", id);
        return repository.findById(id).map(mapper::toDto).orElse(null);
    }

    @Override
    public List<LocalisationDTO> findAll() {
        log.debug("Récupération de toutes les localisations");
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public LocalisationDTO save(LocalisationDTO dto) {
        log.info("Enregistrement localisation pour commerce: {}", dto.getIdcommerce());
        Localisation entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public LocalisationDTO update(UUID id, LocalisationDTO dto) {
        log.info("Mise à jour localisation ID: {}", id);
        if (repository.existsById(id)) {
            Localisation entity = mapper.toEntity(dto);
            entity.setIdlocalisation(id);
            return mapper.toDto(repository.save(entity));
        }
        log.error("Localisation non trouvée pour mise à jour: {}", id);
        return null;
    }

    @Override
    public void delete(UUID id) {
        log.info("Suppression localisation ID: {}", id);
        repository.deleteById(id);
    }

    @Override
    public List<LocalisationDTO> findByCommerce(UUID commerceId) {
        log.debug("Recherche localisations commerce ID: {}", commerceId);
        return repository.findByCommerce_Idcommerce(commerceId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<LocalisationDTO> findByVille(String ville) {
        log.debug("Recherche localisations ville: {}", ville);
        return repository.findByVilleIgnoreCase(ville).stream().map(mapper::toDto).collect(Collectors.toList());
    }
}
