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
    @Query("SELECT c FROM Commerce c JOIN c.abonnement a WHERE c.statut = :statut ORDER BY a.prioriteAffichage DESC, c.dateCreation DESC")
    List<Commerce> findByStatutOrderByPriority(@Param("statut") StatutCommerce statut);

    @Query("SELECT c FROM Commerce c JOIN c.abonnement a WHERE c.nom LIKE %:nom% AND c.statut = 'VALIDE' ORDER BY a.prioriteAffichage DESC")
    List<Commerce> findByNomOrderByPriority(@Param("nom") String nom);

    boolean existsByReference(String reference);

    @Query("SELECT c FROM Commerce c LEFT JOIN FETCH c.abonnement")
    List<Commerce> findAllWithAbonnement();

    @Query(value = "SELECT c.* FROM commerce c " +
                   "JOIN localisation l ON c.idcommerce = l.idcommerce " +
                   "JOIN abonnement a ON c.idabonnement = a.idabonnement " +
                   "WHERE ST_DWithin(l.geolocalisation, ST_SetSRID(ST_Point(:lon, :lat), 4326)::geography, :distance) " +
                   "ORDER BY a.priorite_affichage DESC", 
           nativeQuery = true)
    List<Commerce> findNearby(@Param("lat") double lat, @Param("lon") double lon, @Param("distance") double distance);
}
