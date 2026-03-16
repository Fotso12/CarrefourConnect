package com.carrefourconnect.repositories;

import com.carrefourconnect.entities.Administrateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AdministrateurRepository extends JpaRepository<Administrateur, UUID> {
}
