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
    private TypeAbonnement type;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private BigDecimal montant;
    private StatutAbonnement statut;
}
