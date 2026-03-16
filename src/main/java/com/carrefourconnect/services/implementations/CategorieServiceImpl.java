package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.CategorieDTO;
import com.carrefourconnect.entities.Categorie;
import com.carrefourconnect.mappers.CategorieMapper;
import com.carrefourconnect.repositories.CategorieRepository;
import com.carrefourconnect.services.interfaces.CategorieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategorieServiceImpl implements CategorieService {

    private final CategorieRepository repository;
    private final CategorieMapper mapper;

    @Override
    public CategorieDTO findById(UUID id) {
        return repository.findById(id).map(mapper::toDto).orElse(null);
    }

    @Override
    public List<CategorieDTO> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public CategorieDTO save(CategorieDTO dto) {
        Categorie entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public CategorieDTO update(UUID id, CategorieDTO dto) {
        if (repository.existsById(id)) {
            Categorie entity = mapper.toEntity(dto);
            entity.setIdcategorie(id);
            return mapper.toDto(repository.save(entity));
        }
        return null;
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public CategorieDTO findByNom(String nom) {
        return repository.findByNomIgnoreCase(nom).map(mapper::toDto).orElse(null);
    }
}
