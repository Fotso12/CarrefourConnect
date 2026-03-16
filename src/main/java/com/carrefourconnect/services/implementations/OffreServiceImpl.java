package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.OffreDTO;
import com.carrefourconnect.entities.Offre;
import com.carrefourconnect.mappers.OffreMapper;
import com.carrefourconnect.repositories.OffreRepository;
import com.carrefourconnect.services.interfaces.OffreService;
import com.carrefourconnect.utils.enums.StatutOffre;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OffreServiceImpl implements OffreService {

    private final OffreRepository repository;
    private final OffreMapper mapper;

    @Override
    public OffreDTO findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public List<OffreDTO> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public OffreDTO save(OffreDTO dto) {
        Offre entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public OffreDTO update(UUID id, OffreDTO dto) {
        if (repository.existsById(id)) {
            Offre entity = mapper.toEntity(dto);
            entity.setIdoffre(id);
            return mapper.toDto(repository.save(entity));
        }
        return null;
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public List<OffreDTO> findByCommerce(UUID commerceId) {
        return repository.findByCommerce_Idcommerce(commerceId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OffreDTO> findActiveOffres() {
        LocalDateTime now = LocalDateTime.now();
        return repository.findByDateDebutBeforeAndDateFinAfter(now, now).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OffreDTO> findByStatut(StatutOffre statut) {
        return repository.findByStatut(statut).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
