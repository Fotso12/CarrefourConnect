package com.carrefourconnect.dtos;

import com.carrefourconnect.utils.enums.StatutCommerce;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommerceDTO {
    private UUID idcommerce;
    private UUID idcategorie;
    private UUID idabonnement;
    private UUID iduser; // ID du commercant
    private String nom;
    private String description;
    private String telephone1;
    private String telephone2;
    private String email;
    private String siteweb;
    private StatutCommerce statut;
    private LocalDateTime dateCreation;
    private Long nombreVues;
    private BigDecimal noteGlobale;
    private LocalTime heureOuverture;
    private LocalTime heureFermeture;
    private List<LocalisationDTO> localisations;
    private String imagePrincipale;
}
