package com.carrefourconnect.repositories;

import com.carrefourconnect.entities.Commerce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommerceRepository extends JpaRepository<Commerce, UUID> {
}
