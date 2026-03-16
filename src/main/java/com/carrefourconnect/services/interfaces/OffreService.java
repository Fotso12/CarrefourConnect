package com.carrefourconnect.services.interfaces;

import com.carrefourconnect.dtos.OffreDTO;
import com.carrefourconnect.utils.enums.StatutOffre;
import java.util.List;
import java.util.UUID;

public interface OffreService {
    OffreDTO findById(UUID id);
    List<OffreDTO> findAll();
    OffreDTO save(OffreDTO dto);
    OffreDTO update(UUID id, OffreDTO dto);
    void delete(UUID id);
    
    // Méthodes avancées
    List<OffreDTO> findByCommerce(UUID commerceId);
    List<OffreDTO> findActiveOffres();
    List<OffreDTO> findByStatut(StatutOffre statut);
}
