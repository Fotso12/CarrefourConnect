package com.carrefourconnect.entities;

import com.carrefourconnect.utils.enums.StatutOffre;
import com.carrefourconnect.utils.enums.TypeOffre;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "offre")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offre {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idoffre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcommerce", nullable = false)
    private Commerce commerce;

    @Column(length = 256, nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 64, nullable = false)
    private TypeOffre type;

    @Column(precision = 5, scale = 2)
    private BigDecimal reduction;

    @Column(name = "datedebut", nullable = false)
    private LocalDateTime dateDebut;

    @Column(name = "datefin", nullable = false)
    private LocalDateTime dateFin;

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = false)
    private StatutOffre statut;
}
