package com.carrefourconnect.dtos;

import com.carrefourconnect.utils.enums.StatutOffre;
import com.carrefourconnect.utils.enums.TypeOffre;
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
public class OffreDTO {
    private UUID idoffre;
    private UUID idcommerce;
    private String titre;
    private String description;
    private TypeOffre type;
    private BigDecimal reduction;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private StatutOffre statut;
}
