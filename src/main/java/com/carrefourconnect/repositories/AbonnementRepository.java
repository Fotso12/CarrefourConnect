package com.carrefourconnect.repositories;

import com.carrefourconnect.entities.Abonnement;
import com.carrefourconnect.utils.enums.StatutAbonnement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AbonnementRepository extends JpaRepository<Abonnement, UUID> {
    List<Abonnement> findByStatut(StatutAbonnement statut);
}
