package com.carrefourconnect.services.interfaces;

import com.carrefourconnect.dtos.AbonnementDTO;
import com.carrefourconnect.utils.enums.StatutAbonnement;
import java.util.List;
import java.util.UUID;

public interface AbonnementService {
    AbonnementDTO findById(UUID id);
    List<AbonnementDTO> findAll();
    AbonnementDTO save(AbonnementDTO dto);
    AbonnementDTO update(UUID id, AbonnementDTO dto);
    void delete(UUID id);
    
    // Méthodes avancées
    List<AbonnementDTO> findByStatut(StatutAbonnement statut);
    void updatePrixParType(String type, java.math.BigDecimal prix);
}
