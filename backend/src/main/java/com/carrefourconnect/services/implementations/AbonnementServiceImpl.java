package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.AbonnementDTO;
import com.carrefourconnect.entities.Abonnement;
import com.carrefourconnect.mappers.AbonnementMapper;
import com.carrefourconnect.repositories.AbonnementRepository;
import com.carrefourconnect.services.interfaces.AbonnementService;
import com.carrefourconnect.utils.enums.StatutAbonnement;
import com.carrefourconnect.utils.enums.TypeAbonnement;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AbonnementServiceImpl implements AbonnementService {

    private static final Logger log = LoggerFactory.getLogger(AbonnementServiceImpl.class);

    private final AbonnementRepository repository;
    private final AbonnementMapper mapper;

    public AbonnementServiceImpl(AbonnementRepository repository, AbonnementMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

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

    @Override
    public void updatePrixParType(String type, BigDecimal prix) {
        log.info("Mise à jour du prix pour le type d'abonnement: {} -> {} FCFA", type, prix);
        TypeAbonnement typeEnum = TypeAbonnement.valueOf(type);
        List<Abonnement> abonnements = repository.findAll().stream()
                .filter(a -> typeEnum.equals(a.getType()))
                .collect(Collectors.toList());
        
        abonnements.forEach(a -> {
            a.setMontant(prix);
            repository.save(a);
        });
        log.info("{} abonnement(s) mis à jour pour le type {}", abonnements.size(), type);
    }
}

