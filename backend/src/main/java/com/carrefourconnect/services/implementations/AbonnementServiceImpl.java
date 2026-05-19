package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.AbonnementDTO;
import com.carrefourconnect.dtos.PlanConfigDTO;
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
        List<Abonnement> abonnements = repository.findByType(typeEnum);
        abonnements.forEach(a -> {
            a.setMontant(prix);
            repository.save(a);
        });
        log.info("{} abonnement(s) mis à jour pour le type {}", abonnements.size(), type);
    }

    @Override
    public AbonnementDTO findConfigParType(String type) {
        log.debug("Récupération de la configuration du plan: {}", type);
        TypeAbonnement typeEnum = TypeAbonnement.valueOf(type.toUpperCase());
        return repository.findByType(typeEnum)
                .stream()
                .findFirst()
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public void updateConfigParType(String type, PlanConfigDTO config) {
        log.info("Mise à jour de la configuration du plan: {}", type);
        TypeAbonnement typeEnum = TypeAbonnement.valueOf(type.toUpperCase());
        List<Abonnement> abonnements = repository.findByType(typeEnum);

        if (abonnements.isEmpty()) {
            throw new IllegalArgumentException("Aucun abonnement trouvé pour le type: " + type);
        }

        abonnements.forEach(a -> {
            if (config.getMontant() != null) a.setMontant(config.getMontant());
            a.setMaxPhotos(config.getMaxPhotos());
            a.setOffreSpecialeAutorisee(config.isOffreSpecialeAutorisee());
            a.setMiseEnAvant(config.isMiseEnAvant());
            a.setPrioriteAffichage(config.getPrioriteAffichage());
            a.setLienWhatsapp(config.isLienWhatsapp());
            a.setNotificationPush(config.isNotificationPush());
            if (config.getNomAffiche() != null) a.setNomAffiche(config.getNomAffiche());
            if (config.getDescriptionPlan() != null) a.setDescriptionPlan(config.getDescriptionPlan());
            repository.save(a);
        });

        log.info("{} abonnement(s) mis à jour pour le plan {}", abonnements.size(), type);
    }

    @Override
    public List<AbonnementDTO> findByCommercant(UUID userId) {
        log.debug("Récupération de l'historique d'abonnement pour l'utilisateur: {}", userId);
        return repository.findByCommercantId(userId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
