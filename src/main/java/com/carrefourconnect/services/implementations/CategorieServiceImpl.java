package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.CategorieDTO;
import com.carrefourconnect.entities.Categorie;
import com.carrefourconnect.mappers.CategorieMapper;
import com.carrefourconnect.repositories.CategorieRepository;
import com.carrefourconnect.services.interfaces.CategorieService;
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
public class CategorieServiceImpl implements CategorieService {

    private final CategorieRepository repository;
    private final CategorieMapper mapper;

    @Override
    public CategorieDTO findById(UUID id) {
        log.debug("Récupération catégorie ID: {}", id);
        return repository.findById(id).map(mapper::toDto).orElse(null);
    }

    @Override
    public List<CategorieDTO> findAll() {
        log.debug("Récupération de toutes les catégories");
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public CategorieDTO save(CategorieDTO dto) {
        log.info("Enregistrement catégorie: {}", dto.getNom());
        Categorie entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public CategorieDTO update(UUID id, CategorieDTO dto) {
        log.info("Mise à jour catégorie ID: {}", id);
        if (repository.existsById(id)) {
            Categorie entity = mapper.toEntity(dto);
            entity.setIdcategorie(id);
            return mapper.toDto(repository.save(entity));
        }
        log.error("Catégorie non trouvée pour mise à jour: {}", id);
        return null;
    }

    @Override
    public void delete(UUID id) {
        log.info("Suppression catégorie ID: {}", id);
        repository.deleteById(id);
    }

    @Override
    public CategorieDTO findByNom(String nom) {
        log.debug("Recherche catégorie par nom: {}", nom);
        return repository.findByNomIgnoreCase(nom).map(mapper::toDto).orElse(null);
    }
}
