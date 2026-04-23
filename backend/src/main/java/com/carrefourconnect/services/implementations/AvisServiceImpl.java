package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.AvisDTO;
import com.carrefourconnect.entities.Avis;
import com.carrefourconnect.mappers.AvisMapper;
import com.carrefourconnect.repositories.AvisRepository;
import com.carrefourconnect.repositories.CommerceRepository;
import com.carrefourconnect.repositories.VisiteurRepository;
import com.carrefourconnect.services.interfaces.AvisService;
import com.carrefourconnect.utils.enums.StatutAvis;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AvisServiceImpl implements AvisService {

    private static final Logger log = LoggerFactory.getLogger(AvisServiceImpl.class);

    private final AvisRepository repository;
    private final CommerceRepository commerceRepository;
    private final VisiteurRepository visiteurRepository;
    private final AvisMapper mapper;

    public AvisServiceImpl(AvisRepository repository, CommerceRepository commerceRepository, VisiteurRepository visiteurRepository, AvisMapper mapper) {
        this.repository = repository;
        this.commerceRepository = commerceRepository;
        this.visiteurRepository = visiteurRepository;
        this.mapper = mapper;
    }

    @Override
    public AvisDTO findById(UUID id) {
        log.debug("Récupération avis ID: {}", id);
        return repository.findById(id).map(mapper::toDto).orElse(null);
    }

    @Override
    public List<AvisDTO> findAll() {
        log.debug("Récupération de tous les avis");
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public AvisDTO save(AvisDTO dto) {
        log.info("Nouvel avis pour commerce ID: {}", dto.getIdcommerce());
        Avis entity = mapper.toEntity(dto);
        if (dto.getIdcommerce() != null) {
            entity.setCommerce(commerceRepository.findById(dto.getIdcommerce()).orElseThrow(() -> new RuntimeException("Commerce non trouvé")));
        }
        if (dto.getIduser() != null) {
            entity.setVisiteur(visiteurRepository.findById(dto.getIduser()).orElseThrow(() -> new RuntimeException("Visiteur non trouvé")));
        }
        // Defense-in-depth: prevent a commercant from rating their own commerce
        if (entity.getCommerce() != null && entity.getCommerce().getCommercant() != null && entity.getVisiteur() != null) {
            try {
                if (entity.getVisiteur().getIduser().equals(entity.getCommerce().getCommercant().getIduser())) {
                    throw new RuntimeException("Un commerçant ne peut pas noter son propre commerce");
                }
            } catch (Exception e) {
                log.warn("Vérification propriétaire/commercant échouée: {}", e.getMessage());
                throw e;
            }
        }
        Avis saved = repository.save(entity);
        updateCommerceRating(saved.getCommerce().getIdcommerce());
        return mapper.toDto(saved);
    }

    @Override
    public AvisDTO update(UUID id, AvisDTO dto) {
        log.info("Mise à jour avis ID: {}", id);
        return repository.findById(id).map(existing -> {
            existing.setNote(dto.getNote());
            existing.setCommentaire(dto.getCommentaire());
            existing.setStatus(dto.getStatus());
            
            // Gestion de la réponse du commerçant
            if (dto.getReponse() != null && !dto.getReponse().equals(existing.getReponse())) {
                existing.setReponse(dto.getReponse());
                existing.setDateReponse(LocalDateTime.now());
            }

            Avis saved = repository.save(existing);
            updateCommerceRating(saved.getCommerce().getIdcommerce());
            return mapper.toDto(saved);
        }).orElseGet(() -> {
            log.error("Avis non trouvé pour mise à jour: {}", id);
            return null;
        });
    }

    @Override
    public void delete(UUID id) {
        log.info("Suppression avis ID: {}", id);
        repository.findById(id).ifPresentOrElse(avis -> {
            UUID commerceId = avis.getCommerce().getIdcommerce();
            repository.deleteById(id);
            updateCommerceRating(commerceId);
        }, () -> log.error("Avis non trouvé pour suppression: {}", id));
    }

    @Override
    public List<AvisDTO> findByCommerce(UUID commerceId) {
        log.debug("Recherche avis commerce ID: {}", commerceId);
        return repository.findByCommerce_Idcommerce(commerceId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<AvisDTO> findByVisiteur(UUID visiteurId) {
        log.debug("Recherche avis visiteur ID: {}", visiteurId);
        return repository.findByVisiteur_Iduser(visiteurId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<AvisDTO> findByStatus(StatutAvis status) {
        log.debug("Filtrage avis par statut: {}", status);
        return repository.findByStatus(status).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    private void updateCommerceRating(UUID commerceId) {
        log.debug("Recalcul de la note globale pour le commerce: {}", commerceId);
        BigDecimal average = repository.calculateAverageRating(commerceId);
        commerceRepository.findById(commerceId).ifPresent(commerce -> {
            commerce.setNoteGlobale(average);
            commerceRepository.save(commerce);
        });
    }
}
