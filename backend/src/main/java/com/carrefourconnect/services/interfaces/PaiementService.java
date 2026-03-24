package com.carrefourconnect.services.interfaces;

import com.carrefourconnect.dtos.PaiementDTO;
import com.carrefourconnect.utils.enums.StatutPaiement;

import java.util.List;
import java.util.UUID;

public interface PaiementService {
    PaiementDTO findById(UUID id);
    List<PaiementDTO> findAll();
    PaiementDTO save(PaiementDTO dto);
    PaiementDTO update(UUID id, PaiementDTO dto);
    void delete(UUID id);
    List<PaiementDTO> findByAbonnement(UUID abonnementId);
    PaiementDTO findByReference(String reference);
    PaiementDTO findByNumeroPaiement(String numeroPaiement);
    List<PaiementDTO> findByStatut(StatutPaiement statut);
}
