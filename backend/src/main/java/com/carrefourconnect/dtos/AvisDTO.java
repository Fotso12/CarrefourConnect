package com.carrefourconnect.dtos;

import com.carrefourconnect.utils.enums.StatutAvis;
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
public class AvisDTO {
    private UUID idavis;
    private UUID idcommerce;
    private UUID iduser;
    private BigDecimal note;
    private String commentaire;
    private LocalDateTime datePublication;
    private StatutAvis status;
    private String reponse;
    private LocalDateTime dateReponse;
    private String nomCommerce;
}
