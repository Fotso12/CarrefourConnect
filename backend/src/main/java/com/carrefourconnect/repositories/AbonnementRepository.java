package com.carrefourconnect.repositories;

import com.carrefourconnect.entities.Abonnement;
import com.carrefourconnect.utils.enums.StatutAbonnement;
import com.carrefourconnect.utils.enums.TypeAbonnement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AbonnementRepository extends JpaRepository<Abonnement, UUID> {

    List<Abonnement> findByStatut(StatutAbonnement statut);

    boolean existsByReference(String reference);

    /** Récupère tous les abonnements d'un type donné. */
    List<Abonnement> findByType(TypeAbonnement type);

    /** Récupère l'abonnement de référence par sa clé unique. */
    Optional<Abonnement> findByReference(String reference);

    /** Récupère l'historique des abonnements d'un commerce. */
    List<Abonnement> findByIdCommerceOrderByDateDebutDesc(UUID idCommerce);

    /** Récupère tous les abonnements de tous les commerces d'un utilisateur. */
    @org.springframework.data.jpa.repository.Query("SELECT a FROM Abonnement a WHERE a.idCommerce IN (SELECT c.idcommerce FROM Commerce c WHERE c.commercant.iduser = :userId) ORDER BY a.dateDebut DESC")
    List<Abonnement> findByCommercantId(@org.springframework.data.repository.query.Param("userId") UUID userId);
}
