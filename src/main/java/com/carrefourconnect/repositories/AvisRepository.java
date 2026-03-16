package com.carrefourconnect.repositories;

import com.carrefourconnect.entities.Avis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AvisRepository extends JpaRepository<Avis, UUID> {
}
