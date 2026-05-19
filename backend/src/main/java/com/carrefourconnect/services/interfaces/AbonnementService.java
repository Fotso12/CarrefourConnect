package com.carrefourconnect.services.interfaces;

import com.carrefourconnect.dtos.AbonnementDTO;
import com.carrefourconnect.dtos.PlanConfigDTO;
import com.carrefourconnect.utils.enums.StatutAbonnement;

import java.math.BigDecimal;
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
    void updatePrixParType(String type, BigDecimal prix);

    /**
     * Retourne la configuration du plan de référence pour un type donné.
     * Utilisé par le frontend pour appliquer les restrictions dynamiquement.
     */
    AbonnementDTO findConfigParType(String type);

    /**
     * Met à jour la configuration complète (droits + prix) de tous les abonnements
     * d'un type donné. Action réservée à l'admin.
     */
    void updateConfigParType(String type, PlanConfigDTO config);

    /** Récupère tout l'historique d'abonnement pour un commerçant (tous ses commerces). */
    List<AbonnementDTO> findByCommercant(UUID userId);
}
