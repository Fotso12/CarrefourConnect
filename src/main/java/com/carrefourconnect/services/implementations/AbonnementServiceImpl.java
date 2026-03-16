package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.AbonnementDTO;
import com.carrefourconnect.entities.Abonnement;
import com.carrefourconnect.mappers.AbonnementMapper;
import com.carrefourconnect.repositories.AbonnementRepository;
import com.carrefourconnect.services.interfaces.AbonnementService;
import com.carrefourconnect.utils.enums.StatutAbonnement;
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
public class AbonnementServiceImpl implements AbonnementService {

    private final AbonnementRepository repository;
    private final AbonnementMapper mapper;

    @Override
    public AbonnementDTO findById(UUID id) {
        log.debug("Récupération abonnement ID: {}", id);
        return repository.findById(id).map(mapper::toDto).orElse(null);
    }

    @Override
    public List<AbonnementDTO> findAll() {
        log.debug("Récupération de tous les abonnements");
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public AbonnementDTO save(AbonnementDTO dto) {
        log.info("Création d'un nouvel abonnement type: {}", dto.getType());
        Abonnement entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public AbonnementDTO update(UUID id, AbonnementDTO dto) {
        log.info("Mise à jour de l'abonnement ID: {}", id);
        if (repository.existsById(id)) {
            Abonnement entity = mapper.toEntity(dto);
            entity.setIdabonnement(id);
            return mapper.toDto(repository.save(entity));
        }
        log.error("Abonnement non trouvé pour mise à jour: {}", id);
        return null;
    }

    @Override
    public void delete(UUID id) {
        log.info("Suppression de l'abonnement ID: {}", id);
        repository.deleteById(id);
    }

    @Override
    public List<AbonnementDTO> findByStatut(StatutAbonnement statut) {
        log.debug("Filtrage des abonnements par statut: {}", statut);
        return repository.findByStatut(statut).stream().map(mapper::toDto).collect(Collectors.toList());
    }
}
