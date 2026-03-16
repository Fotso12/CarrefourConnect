package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.AvisDTO;
import com.carrefourconnect.entities.Avis;
import com.carrefourconnect.mappers.AvisMapper;
import com.carrefourconnect.repositories.AvisRepository;
import com.carrefourconnect.services.interfaces.AvisService;
import com.carrefourconnect.utils.enums.StatutAvis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AvisServiceImpl implements AvisService {

    private final AvisRepository repository;
    private final AvisMapper mapper;

    @Override
    public AvisDTO findById(UUID id) {
        return repository.findById(id).map(mapper::toDto).orElse(null);
    }

    @Override
    public List<AvisDTO> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public AvisDTO save(AvisDTO dto) {
        Avis entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public AvisDTO update(UUID id, AvisDTO dto) {
        if (repository.existsById(id)) {
            Avis entity = mapper.toEntity(dto);
            entity.setIdavis(id);
            return mapper.toDto(repository.save(entity));
        }
        return null;
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public List<AvisDTO> findByCommerce(UUID commerceId) {
        return repository.findByCommerce_Idcommerce(commerceId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<AvisDTO> findByVisiteur(UUID visiteurId) {
        return repository.findByVisiteur_Iduser(visiteurId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<AvisDTO> findByStatus(StatutAvis status) {
        return repository.findByStatus(status).stream().map(mapper::toDto).collect(Collectors.toList());
    }
}
