package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.OffreDTO;
import com.carrefourconnect.entities.Offre;
import com.carrefourconnect.entities.Commerce;
import com.carrefourconnect.mappers.OffreMapper;
import com.carrefourconnect.repositories.OffreRepository;
import com.carrefourconnect.repositories.CommerceRepository;
import com.carrefourconnect.services.interfaces.OffreService;
import com.carrefourconnect.utils.enums.StatutOffre;
import com.carrefourconnect.utils.enums.TypeOffre;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OffreServiceImpl implements OffreService {

    private static final Logger log = LoggerFactory.getLogger(OffreServiceImpl.class);

    private final OffreRepository repository;
    private final CommerceRepository commerceRepository;
    private final OffreMapper mapper;
    private final FcmService fcmService;

    public OffreServiceImpl(OffreRepository repository, CommerceRepository commerceRepository, OffreMapper mapper, FcmService fcmService) {
        this.repository = repository;
        this.commerceRepository = commerceRepository;
        this.mapper = mapper;
        this.fcmService = fcmService;
    }

    @Override
    public OffreDTO findById(UUID id) {
        log.debug("Récupération offre ID: {}", id);
        return repository.findById(id).map(mapper::toDto).orElse(null);
    }

    @Override
    public List<OffreDTO> findAll() {
        log.debug("Récupération de toutes les offres");
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public OffreDTO save(OffreDTO dto) {
        log.info("Création d'une nouvelle offre: {}", dto.getTitre());
        Offre entity = mapper.toEntity(dto);
        
        Commerce commerce = null;
        if (dto.getIdcommerce() != null) {
            commerce = commerceRepository.findById(dto.getIdcommerce())
                    .orElseThrow(() -> new RuntimeException("Commerce non trouvé"));
            
            // Vérification des droits d'abonnement
            if (commerce.getAbonnement() != null && !commerce.getAbonnement().isOffreSpecialeAutorisee()) {
                log.warn("Tentative de création d'offre pour un commerce dont l'abonnement ne l'autorise pas: {}", commerce.getNom());
                throw new IllegalStateException("Votre abonnement actuel ne vous permet pas de créer des offres spéciales.");
            }
            
            entity.setCommerce(commerce);
        } else {
            throw new IllegalArgumentException("L'ID du commerce est obligatoire pour créer une offre");
        }
        
        // Valeurs par défaut pour les champs obligatoires non envoyés par le frontend
        if (entity.getStatut() == null) {
            entity.setStatut(StatutOffre.ACTIF);
        }
        if (entity.getType() == null) {
            entity.setType(TypeOffre.PROMOTION);
        }
        if (entity.getDateDebut() == null) {
            entity.setDateDebut(LocalDateTime.now());
        }
        if (entity.getDateFin() == null) {
            entity.setDateFin(LocalDateTime.now().plusDays(30));
        }
        
        Offre savedOffre = repository.save(entity);

        // Envoyer des notifications push si l'abonnement le permet
        if (commerce != null && commerce.getAbonnement() != null && commerce.getAbonnement().isNotificationPush()) {
            envoyerNotificationsPush(commerce, savedOffre);
        }

        return mapper.toDto(savedOffre);
    }

    private void envoyerNotificationsPush(Commerce commerce, Offre offre) {
        log.info("Envoi de notifications push pour l'offre: {}", offre.getTitre());
        
        String title = "Nouvelle offre chez " + commerce.getNom() + " !";
        String body = offre.getTitre() + " : " + offre.getDescription();
        java.util.Map<String, String> data = new java.util.HashMap<>();
        data.put("type", "NOUVELLE_OFFRE");
        data.put("idcommerce", commerce.getIdcommerce().toString());
        data.put("idoffre", offre.getIdoffre().toString());

        // On pourrait utiliser topic "commerce_" + commerce.getIdcommerce()
        // Mais ici on boucle sur les favoris pour l'exemple, ou on utilise le topic.
        // Utiliser un topic est plus efficace pour un grand nombre d'utilisateurs.
        fcmService.sendTopicNotification("commerce_" + commerce.getIdcommerce(), title, body, data);
    }

    @Override
    public OffreDTO update(UUID id, OffreDTO dto) {
        log.info("Mise à jour de l'offre ID: {}", id);
        if (repository.existsById(id)) {
            Offre entity = mapper.toEntity(dto);
            entity.setIdoffre(id);
            return mapper.toDto(repository.save(entity));
        }
        log.error("Offre non trouvée pour mise à jour: {}", id);
        return null;
    }

    @Override
    public void delete(UUID id) {
        log.info("Suppression de l'offre ID: {}", id);
        repository.deleteById(id);
    }

    @Override
    public List<OffreDTO> findByCommerce(UUID commerceId) {
        log.debug("Récupération offres pour commerce ID: {}", commerceId);
        return repository.findByCommerce_Idcommerce(commerceId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<OffreDTO> findActiveOffres() {
        log.debug("Recherche des offres actives");
        return repository.findActiveOffres().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<OffreDTO> findByStatut(StatutOffre statut) {
        log.debug("Filtrage offres par statut: {}", statut);
        return repository.findByStatut(statut).stream().map(mapper::toDto).collect(Collectors.toList());
    }
}
