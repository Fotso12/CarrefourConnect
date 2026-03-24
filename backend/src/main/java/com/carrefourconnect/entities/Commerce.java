package com.carrefourconnect.entities;

import com.carrefourconnect.utils.enums.StatutCommerce;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "commerce")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commerce {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idcommerce;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcategorie", nullable = false)
    private Categorie categorie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idabonnement", nullable = false)
    private Abonnement abonnement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iduser", nullable = false)
    private Commercant commercant;

    @Column(length = 256, nullable = false)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 20, nullable = false)
    private String telephone1;

    @Column(length = 20)
    private String telephone2;

    @Column(length = 256)
    private String email;

    @Column(length = 512)
    private String siteweb;

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = false)
    private StatutCommerce statut;

    @CreationTimestamp
    @Column(name = "datecreation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "nombrevues", nullable = false)
    private Long nombreVues = 0L;

    @Column(name = "noteglobale", precision = 3, scale = 2)
    private BigDecimal noteGlobale;

    @ManyToMany(mappedBy = "favoris", fetch = FetchType.LAZY)
    private Set<Utilisateur> utilisateursFavoris = new HashSet<>();

    @Column(length = 64, unique = true)
    private String reference;

    @Column(name = "heureouverture")
    private LocalTime heureOuverture;

    @Column(name = "heurefermeture")
    private LocalTime heureFermeture;

    @OneToMany(mappedBy = "commerce", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Localisation> localisations = new ArrayList<>();
}
