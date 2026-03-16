package com.carrefourconnect.repositories;

import com.carrefourconnect.entities.Abonnement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AbonnementRepository extends JpaRepository<Abonnement, UUID> {
}
