package com.carrefourconnect.dtos;

import com.carrefourconnect.utils.enums.StatutPaiement;
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
public class PaiementDTO {
    private UUID idpaiement;
    private UUID idabonnement;
    private BigDecimal montant;
    private LocalDateTime datePaiement;
    private String modePaiement;
    private String reference;
    private StatutPaiement statut;
    private String numeroPaiement;
}
