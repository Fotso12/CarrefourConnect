package com.carrefourconnect.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "media")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idmedia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcommerce", nullable = false)
    private Commerce commerce;

    @Column(length = 512, nullable = false)
    private String url;

    @Column(length = 256, nullable = false)
    private String nom;

    @Column(name = "typecontenu", length = 128, nullable = false)
    private String typeContenu;

    @Column(name = "taillefichier", nullable = false)
    private Long tailleFichier;

    @Column(name = "estprincipale", nullable = false)
    private boolean estPrincipale = false;
}
