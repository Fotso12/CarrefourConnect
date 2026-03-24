package com.carrefourconnect.repositories;

import com.carrefourconnect.entities.Commerce;
import com.carrefourconnect.utils.enums.StatutCommerce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommerceRepository extends JpaRepository<Commerce, UUID> {
    List<Commerce> findByCategorie_Idcategorie(UUID idCategorie);
    List<Commerce> findByCommercant_Iduser(UUID idUser);
    List<Commerce> findByNomContainingIgnoreCase(String nom);
    List<Commerce> findByStatut(StatutCommerce statut);
    boolean existsByReference(String reference);

    @Query(value = "SELECT c.* FROM commerce c " +
                   "JOIN localisation l ON c.idcommerce = l.idcommerce " +
                   "WHERE ST_DWithin(l.geolocalisation, ST_SetSRID(ST_Point(:lon, :lat), 4326)::geography, :distance)", 
           nativeQuery = true)
    List<Commerce> findNearby(@Param("lat") double lat, @Param("lon") double lon, @Param("distance") double distance);
}
