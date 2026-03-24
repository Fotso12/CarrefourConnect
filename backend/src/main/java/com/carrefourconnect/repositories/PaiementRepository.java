package com.carrefourconnect.repositories;

import com.carrefourconnect.entities.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carrefourconnect.utils.enums.StatutPaiement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, UUID> {
    List<Paiement> findByAbonnement_Idabonnement(UUID idabonnement);
    Optional<Paiement> findByReference(String reference);
    Optional<Paiement> findByNumeroPaiement(String numeroPaiement);
    List<Paiement> findByStatut(StatutPaiement statut);
    boolean existsByNumeroPaiement(String numeroPaiement);
}
