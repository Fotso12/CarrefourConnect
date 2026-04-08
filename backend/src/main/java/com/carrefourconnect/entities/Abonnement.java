package com.carrefourconnect.entities;

import com.carrefourconnect.utils.enums.StatutAbonnement;
import com.carrefourconnect.utils.enums.TypeAbonnement;
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
@Table(name = "abonnement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Abonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idabonnement;

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = false)
    private TypeAbonnement type;

    @CreationTimestamp
    @Column(name = "datedebut", nullable = false, updatable = false)
    private LocalDateTime dateDebut;

    @Column(name = "datefin", nullable = false)
    private LocalDateTime dateFin;

    @Builder.Default
    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal montant = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = false)
    private StatutAbonnement statut;

    @Column(length = 64, unique = true)
    private String reference;
}
