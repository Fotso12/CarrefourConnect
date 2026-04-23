package com.carrefourconnect.services.interfaces;

import com.carrefourconnect.dtos.UtilisateurDTO;
import com.carrefourconnect.dtos.VisiteurDTO;
import com.carrefourconnect.dtos.CommercantDTO;
import java.util.List;
import java.util.UUID;

public interface UtilisateurService {
    UtilisateurDTO findById(UUID id);
    List<UtilisateurDTO> findAll();
    List<UtilisateurDTO> findAllNonAdmins();
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

    void suspendre(UUID id, String motif);
    void activer(UUID id);
    // Réinitialisation de mot de passe
    boolean reinitialiserMotDePasseParEmail(String email, String nouveauMotDePasse);
    
    /**
     * Change le mot de passe pour un utilisateur donné en vérifiant l'ancien mot de passe.
     * @param id ID de l'utilisateur
     * @param ancienMotDePasse le mot de passe actuel fourni par l'utilisateur
     * @param nouveauMotDePasse le nouveau mot de passe à appliquer
     * @return true si le changement a réussi, false sinon
     */
    boolean changePassword(java.util.UUID id, String ancienMotDePasse, String nouveauMotDePasse);

}
