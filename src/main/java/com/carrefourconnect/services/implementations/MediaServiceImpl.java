package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.MediaDTO;
import com.carrefourconnect.entities.Media;
import com.carrefourconnect.mappers.MediaMapper;
import com.carrefourconnect.repositories.MediaRepository;
import com.carrefourconnect.services.interfaces.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MediaServiceImpl implements MediaService {

    private final MediaRepository repository;
    private final MediaMapper mapper;

    @Override
    public MediaDTO findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public List<MediaDTO> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public MediaDTO save(MediaDTO dto) {
        Media entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public MediaDTO update(UUID id, MediaDTO dto) {
        return repository.findById(id).map(existing -> {
            existing.setNom(dto.getNom());
            existing.setUrl(dto.getUrl());
            existing.setTypeContenu(dto.getTypeContenu());
            existing.setTailleFichier(dto.getTailleFichier());
            return mapper.toDto(repository.save(existing));
        }).orElse(null);
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public List<MediaDTO> findByCommerce(UUID commerceId) {
        return repository.findByCommerce_Idcommerce(commerceId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
