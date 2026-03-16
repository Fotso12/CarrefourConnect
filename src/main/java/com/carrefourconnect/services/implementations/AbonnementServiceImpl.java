package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.AbonnementDTO;
import com.carrefourconnect.entities.Abonnement;
import com.carrefourconnect.mappers.AbonnementMapper;
import com.carrefourconnect.repositories.AbonnementRepository;
import com.carrefourconnect.services.interfaces.AbonnementService;
import com.carrefourconnect.utils.enums.StatutAbonnement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AbonnementServiceImpl implements AbonnementService {

    private final AbonnementRepository repository;
    private final AbonnementMapper mapper;

    @Override
    public AbonnementDTO findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public List<AbonnementDTO> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AbonnementDTO save(AbonnementDTO dto) {
        Abonnement entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public AbonnementDTO update(UUID id, AbonnementDTO dto) {
        if (repository.existsById(id)) {
            Abonnement entity = mapper.toEntity(dto);
            entity.setIdabonnement(id);
            return mapper.toDto(repository.save(entity));
        }
        return null;
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public List<AbonnementDTO> findByStatut(StatutAbonnement statut) {
        return repository.findByStatut(statut).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
