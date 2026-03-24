package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.MediaDTO;
import com.carrefourconnect.entities.Media;
import com.carrefourconnect.mappers.MediaMapper;
import com.carrefourconnect.repositories.MediaRepository;
import com.carrefourconnect.services.interfaces.MediaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MediaServiceImpl implements MediaService {

    private static final Logger log = LoggerFactory.getLogger(MediaServiceImpl.class);

    private final MediaRepository repository;
    private final MediaMapper mapper;

    public MediaServiceImpl(MediaRepository repository, MediaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public MediaDTO findById(UUID id) {
        log.debug("Récupération média ID: {}", id);
        return repository.findById(id)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public List<MediaDTO> findAll() {
        log.debug("Récupération de tous les médias");
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public MediaDTO save(MediaDTO dto) {
        log.info("Enregistrement média: {}", dto.getNom());
        Media entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public MediaDTO update(UUID id, MediaDTO dto) {
        log.info("Mise à jour média ID: {}", id);
        return repository.findById(id).map(existing -> {
            existing.setNom(dto.getNom());
            existing.setUrl(dto.getUrl());
            existing.setTypeContenu(dto.getTypeContenu());
            existing.setTailleFichier(dto.getTailleFichier());
            return mapper.toDto(repository.save(existing));
        }).orElseGet(() -> {
            log.error("Média non trouvé pour mise à jour: {}", id);
            return null;
        });
    }

    @Override
    public void delete(UUID id) {
        log.info("Suppression média ID: {}", id);
        repository.deleteById(id);
    }

    @Override
    public List<MediaDTO> findByCommerce(UUID commerceId) {
        log.debug("Recherche médias commerce ID: {}", commerceId);
        return repository.findByCommerce_Idcommerce(commerceId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
