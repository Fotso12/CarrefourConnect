package com.carrefourconnect.services.interfaces;

import com.carrefourconnect.dtos.CategorieDTO;
import java.util.List;
import java.util.UUID;

public interface CategorieService {
    CategorieDTO findById(UUID id);
    List<CategorieDTO> findAll();
    CategorieDTO save(CategorieDTO dto);
    CategorieDTO update(UUID id, CategorieDTO dto);
    void delete(UUID id);
    
    // Méthodes avancées
    CategorieDTO findByNom(String nom);
}
