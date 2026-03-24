package com.carrefourconnect.services.interfaces;

import com.carrefourconnect.dtos.LocalisationDTO;
import java.util.List;
import java.util.UUID;

public interface LocalisationService {
    LocalisationDTO findById(UUID id);
    List<LocalisationDTO> findAll();
    LocalisationDTO save(LocalisationDTO dto);
    LocalisationDTO update(UUID id, LocalisationDTO dto);
    void delete(UUID id);
    
    // Méthodes avancées
    List<LocalisationDTO> findByCommerce(UUID commerceId);
    List<LocalisationDTO> findByVille(String ville);
}
