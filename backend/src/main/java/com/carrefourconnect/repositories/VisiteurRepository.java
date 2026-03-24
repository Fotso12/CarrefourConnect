package com.carrefourconnect.repositories;

import com.carrefourconnect.entities.Visiteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VisiteurRepository extends JpaRepository<Visiteur, UUID> {
}
