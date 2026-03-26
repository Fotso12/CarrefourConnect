package com.carrefourconnect.services.interfaces;

import com.carrefourconnect.dtos.CommerceDTO;
import com.carrefourconnect.utils.enums.StatutCommerce;
import java.util.List;
import java.util.UUID;

public interface CommerceService {
    CommerceDTO findById(UUID id);
    List<CommerceDTO> findAll();
    CommerceDTO save(CommerceDTO dto);
    CommerceDTO update(UUID id, CommerceDTO dto);
    void delete(UUID id);
    
    // Méthodes avancées
    List<CommerceDTO> findByCategorie(UUID categorieId);
    List<CommerceDTO> findByCommercant(UUID commercantId);
    List<CommerceDTO> searchByName(String name);
    List<CommerceDTO> findByStatut(StatutCommerce statut);
    List<CommerceDTO> findNearby(double latitude, double longitude, double distanceInKm);
    List<CommerceDTO> rechercher(String nom, UUID idCategorie, String ville, StatutCommerce statut, Double lat, Double lon, Double rayonKm);
    void suspendre(UUID id, String motif);
    void valider(UUID id);
}
