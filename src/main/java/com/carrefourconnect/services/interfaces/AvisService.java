package com.carrefourconnect.services.interfaces;

import com.carrefourconnect.dtos.AvisDTO;
import com.carrefourconnect.utils.enums.StatutAvis;
import java.util.List;
import java.util.UUID;

public interface AvisService {
    AvisDTO findById(UUID id);
    List<AvisDTO> findAll();
    AvisDTO save(AvisDTO dto);
    AvisDTO update(UUID id, AvisDTO dto);
    void delete(UUID id);
    
    // Méthodes avancées
    List<AvisDTO> findByCommerce(UUID commerceId);
    List<AvisDTO> findByVisiteur(UUID visiteurId);
    List<AvisDTO> findByStatus(StatutAvis status);
}
