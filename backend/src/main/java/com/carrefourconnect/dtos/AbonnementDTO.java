package com.carrefourconnect.dtos;

import com.carrefourconnect.utils.enums.StatutAbonnement;
import com.carrefourconnect.utils.enums.TypeAbonnement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbonnementDTO {
    private UUID idabonnement;
    private UUID idCommerce;
    private TypeAbonnement type;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private BigDecimal montant;
    private StatutAbonnement statut;
    private String reference;

    // ===== Droits et restrictions =====
    private int maxPhotos;
    private boolean offreSpecialeAutorisee;
    private boolean miseEnAvant;
    private int prioriteAffichage;
    private boolean lienWhatsapp;
    private boolean notificationPush;
    private String nomAffiche;
    private String descriptionPlan;
}
