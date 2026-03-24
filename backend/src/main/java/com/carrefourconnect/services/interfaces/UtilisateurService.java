package com.carrefourconnect.services.interfaces;

import com.carrefourconnect.dtos.UtilisateurDTO;
import com.carrefourconnect.dtos.VisiteurDTO;
import com.carrefourconnect.dtos.CommercantDTO;
import java.util.List;
import java.util.UUID;

public interface UtilisateurService {
    UtilisateurDTO findById(UUID id);
    List<UtilisateurDTO> findAll();
    UtilisateurDTO save(UtilisateurDTO dto);
    UtilisateurDTO update(UUID id, UtilisateurDTO dto);
    void delete(UUID id);
    
    // Méthodes avancées
    UtilisateurDTO findByEmail(String email);
    void addFavorite(UUID userId, UUID commerceId);
    void removeFavorite(UUID userId, UUID commerceId);
    List<UUID> getFavorites(UUID userId);

    // Inscriptions
    UtilisateurDTO registerVisiteur(VisiteurDTO dto);
    UtilisateurDTO registerCommercant(CommercantDTO dto);
}
