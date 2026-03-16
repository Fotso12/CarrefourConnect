package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.LocalisationDTO;
import com.carrefourconnect.entities.Localisation;
import com.carrefourconnect.mappers.LocalisationMapper;
import com.carrefourconnect.repositories.LocalisationRepository;
import com.carrefourconnect.services.interfaces.LocalisationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LocalisationServiceImpl implements LocalisationService {

    private final LocalisationRepository repository;
    private final LocalisationMapper mapper;

    @Override
    public LocalisationDTO findById(UUID id) {
        return repository.findById(id).map(mapper::toDto).orElse(null);
    }

    @Override
    public List<LocalisationDTO> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public LocalisationDTO save(LocalisationDTO dto) {
        Localisation entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public LocalisationDTO update(UUID id, LocalisationDTO dto) {
        if (repository.existsById(id)) {
            Localisation entity = mapper.toEntity(dto);
            entity.setIdlocalisation(id);
            return mapper.toDto(repository.save(entity));
        }
        return null;
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public List<LocalisationDTO> findByCommerce(UUID commerceId) {
        return repository.findByCommerce_Idcommerce(commerceId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<LocalisationDTO> findByVille(String ville) {
        return repository.findByVilleIgnoreCase(ville).stream().map(mapper::toDto).collect(Collectors.toList());
    }
}
