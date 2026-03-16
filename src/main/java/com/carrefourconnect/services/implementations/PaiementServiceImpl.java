package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.PaiementDTO;
import com.carrefourconnect.entities.Paiement;
import com.carrefourconnect.mappers.PaiementMapper;
import com.carrefourconnect.repositories.PaiementRepository;
import com.carrefourconnect.services.interfaces.PaiementService;
import com.carrefourconnect.utils.enums.StatutPaiement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaiementServiceImpl implements PaiementService {

    private final PaiementRepository repository;
    private final PaiementMapper mapper;

    @Override
    public PaiementDTO findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public List<PaiementDTO> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PaiementDTO save(PaiementDTO dto) {
        Paiement entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public PaiementDTO update(UUID id, PaiementDTO dto) {
        return repository.findById(id).map(existing -> {
            existing.setMontant(dto.getMontant());
            existing.setModePaiement(dto.getModePaiement());
            existing.setStatut(dto.getStatut());
            existing.setReference(dto.getReference());
            return mapper.toDto(repository.save(existing));
        }).orElse(null);
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public List<PaiementDTO> findByAbonnement(UUID abonnementId) {
        return repository.findByAbonnement_Idabonnement(abonnementId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PaiementDTO findByReference(String reference) {
        return repository.findByReference(reference)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public PaiementDTO findByNumeroPaiement(String numeroPaiement) {
        return repository.findByNumeroPaiement(numeroPaiement)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public List<PaiementDTO> findByStatut(StatutPaiement statut) {
        return repository.findByStatut(statut).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
