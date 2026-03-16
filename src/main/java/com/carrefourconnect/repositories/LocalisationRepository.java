package com.carrefourconnect.repositories;

import com.carrefourconnect.entities.Localisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LocalisationRepository extends JpaRepository<Localisation, UUID> {
}
