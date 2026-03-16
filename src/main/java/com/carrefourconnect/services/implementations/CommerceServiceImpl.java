package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.CommerceDTO;
import com.carrefourconnect.entities.Commerce;
import com.carrefourconnect.mappers.CommerceMapper;
import com.carrefourconnect.repositories.CommerceRepository;
import com.carrefourconnect.services.interfaces.CommerceService;
import com.carrefourconnect.utils.enums.StatutCommerce;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommerceServiceImpl implements CommerceService {

    private final CommerceRepository repository;
    private final CommerceMapper mapper;

    @Override
    public CommerceDTO findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public List<CommerceDTO> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommerceDTO save(CommerceDTO dto) {
        Commerce entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public CommerceDTO update(UUID id, CommerceDTO dto) {
        if (repository.existsById(id)) {
            Commerce entity = mapper.toEntity(dto);
            entity.setIdcommerce(id);
            return mapper.toDto(repository.save(entity));
        }
        return null;
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public List<CommerceDTO> findByCategorie(UUID categorieId) {
        return repository.findByCategorie_Idcategorie(categorieId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommerceDTO> findByCommercant(UUID commercantId) {
        return repository.findByCommercant_Iduser(commercantId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommerceDTO> searchByName(String name) {
        return repository.findByNomContainingIgnoreCase(name).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommerceDTO> findByStatut(StatutCommerce statut) {
        return repository.findByStatut(statut).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
