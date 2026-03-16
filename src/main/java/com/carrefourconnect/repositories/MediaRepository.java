package com.carrefourconnect.repositories;

import com.carrefourconnect.entities.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MediaRepository extends JpaRepository<Media, UUID> {
    List<Media> findByCommerce_Idcommerce(UUID idcommerce);
}
