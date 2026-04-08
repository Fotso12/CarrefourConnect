package com.carrefourconnect.entities;

import com.carrefourconnect.utils.enums.StatutAvis;
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
@Table(name = "avis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idavis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcommerce", nullable = false)
    private Commerce commerce;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iduser", nullable = false)
    private Visiteur visiteur;

    @Column(precision = 2, scale = 1, nullable = false)
    private BigDecimal note;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @CreationTimestamp
    @Column(name = "datepublication", nullable = false, updatable = false)
    private LocalDateTime datePublication;

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = false)
    private StatutAvis status;
}
