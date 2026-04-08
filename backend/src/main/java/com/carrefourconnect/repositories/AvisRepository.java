package com.carrefourconnect.repositories;

import com.carrefourconnect.entities.Avis;
import com.carrefourconnect.utils.enums.StatutAvis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface AvisRepository extends JpaRepository<Avis, UUID> {
    List<Avis> findByCommerce_Idcommerce(UUID idCommerce);
    List<Avis> findByVisiteur_Iduser(UUID idUser);
    List<Avis> findByStatus(StatutAvis status);

    @Query("SELECT AVG(a.note) FROM Avis a WHERE a.commerce.idcommerce = :idCommerce AND a.status = 'PUBLIE'")
    BigDecimal calculateAverageRating(@Param("idCommerce") UUID idCommerce);
}
