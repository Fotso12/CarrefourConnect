package com.carrefourconnect.repositories;

import com.carrefourconnect.entities.Commerce;
import com.carrefourconnect.utils.enums.StatutCommerce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommerceRepository extends JpaRepository<Commerce, UUID> {
    List<Commerce> findByCategorie_Idcategorie(UUID idCategorie);
    List<Commerce> findByCommercant_Iduser(UUID idUser);
    List<Commerce> findByNomContainingIgnoreCase(String nom);
    List<Commerce> findByStatut(StatutCommerce statut);
}
