package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.PaiementDTO;
import com.carrefourconnect.entities.Paiement;
import com.carrefourconnect.mappers.PaiementMapper;
import com.carrefourconnect.repositories.PaiementRepository;
import com.carrefourconnect.services.interfaces.PaiementService;
import com.carrefourconnect.utils.enums.StatutPaiement;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaiementServiceImpl implements PaiementService {

    private static final Logger log = LoggerFactory.getLogger(PaiementServiceImpl.class);

    private final PaiementRepository repository;
    private final PaiementMapper mapper;

    public PaiementServiceImpl(PaiementRepository repository, PaiementMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PaiementDTO findById(UUID id) {
        log.debug("Récupération paiement ID: {}", id);
        return repository.findById(id)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public List<PaiementDTO> findAll() {
        log.debug("Récupération de tous les paiements");
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PaiementDTO save(PaiementDTO dto) {
        log.info("Enregistrement opération de paiement: {}", dto.getReference());
        Paiement entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public PaiementDTO update(UUID id, PaiementDTO dto) {
        log.info("Mise à jour paiement ID: {}", id);
        return repository.findById(id).map(existing -> {
            existing.setMontant(dto.getMontant());
            existing.setModePaiement(dto.getModePaiement());
            existing.setStatut(dto.getStatut());
            existing.setReference(dto.getReference());
            return mapper.toDto(repository.save(existing));
        }).orElseGet(() -> {
            log.error("Paiement non trouvé pour mise à jour: {}", id);
            return null;
        });
    }

    @Override
    public void delete(UUID id) {
        log.info("Suppression paiement ID: {}", id);
        repository.deleteById(id);
    }

    @Override
    public List<PaiementDTO> findByAbonnement(UUID abonnementId) {
        log.debug("Recherche paiements abonnement ID: {}", abonnementId);
        return repository.findByAbonnement_Idabonnement(abonnementId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PaiementDTO findByReference(String reference) {
        log.debug("Recherche paiement référence: {}", reference);
        return repository.findByReference(reference)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public PaiementDTO findByNumeroPaiement(String numeroPaiement) {
        log.debug("Recherche paiement numéro: {}", numeroPaiement);
        return repository.findByNumeroPaiement(numeroPaiement)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public List<PaiementDTO> findByStatut(StatutPaiement statut) {
        log.debug("Filtrage paiements statut: {}", statut);
        return repository.findByStatut(statut).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
