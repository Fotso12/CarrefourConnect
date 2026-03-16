package com.carrefourconnect.repositories;

import com.carrefourconnect.entities.Offre;
import com.carrefourconnect.utils.enums.StatutOffre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OffreRepository extends JpaRepository<Offre, UUID> {
    List<Offre> findByCommerce_Idcommerce(UUID idCommerce);
    List<Offre> findByStatut(StatutOffre statut);
    boolean existsByReference(String reference);
    
    @org.springframework.data.jpa.repository.Query("SELECT o FROM Offre o WHERE o.dateDebut <= CURRENT_TIMESTAMP AND o.dateFin >= CURRENT_TIMESTAMP AND o.statut = 'ACTIVE'")
    List<Offre> findActiveOffres();

    List<Offre> findByDateDebutBeforeAndDateFinAfter(LocalDateTime now1, LocalDateTime now2);
}
