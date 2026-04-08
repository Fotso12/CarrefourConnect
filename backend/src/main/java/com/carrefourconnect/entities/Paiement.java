package com.carrefourconnect.entities;

import com.carrefourconnect.utils.enums.StatutPaiement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "paiement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idpaiement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idabonnement", nullable = false)
    private Abonnement abonnement;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal montant;

    @CreationTimestamp
    @Column(name = "datepaiement", nullable = false, updatable = false)
    private LocalDateTime datePaiement;

    @Column(name = "modepaiement", length = 64, nullable = false)
    private String modePaiement;

    @Column(length = 128, nullable = false)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = false)
    private StatutPaiement statut;

    @Column(name = "numeropaiement", length = 64, nullable = false, unique = true)
    private String numeroPaiement;
}
