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

    @Column(name = "idcommerce")
    private UUID idCommerce;

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

    // ===== Droits et restrictions du plan =====
    // Colonnes nullable=false retirées pour permettre la migration Hibernate
    // sur une table existante (les valeurs par défaut sont gérées par le DataInitializer)

    /** Nombre maximum de photos autorisées. -1 = illimité. */
    @Builder.Default
    @Column(name = "max_photos", columnDefinition = "INTEGER DEFAULT 3")
    private int maxPhotos = 3;

    /** Autorisation de créer des offres spéciales (promotions, ventes flash...). */
    @Builder.Default
    @Column(name = "offre_speciale_autorisee", columnDefinition = "BOOLEAN DEFAULT false")
    private boolean offreSpecialeAutorisee = false;

    /** Mise en avant du commerce dans les résultats de recherche. */
    @Builder.Default
    @Column(name = "mise_en_avant", columnDefinition = "BOOLEAN DEFAULT false")
    private boolean miseEnAvant = false;

    /**
     * Priorité d'affichage dans la liste des commerces.
     * 1 = Basique (bas), 2 = Premium (normal), 3 = Gold (VIP / haute)
     */
    @Builder.Default
    @Column(name = "priorite_affichage", columnDefinition = "INTEGER DEFAULT 1")
    private int prioriteAffichage = 1;

    /** Accès au lien WhatsApp direct sur la fiche commerce. */
    @Builder.Default
    @Column(name = "lien_whatsapp", columnDefinition = "BOOLEAN DEFAULT false")
    private boolean lienWhatsapp = false;

    /** Envoi de notifications push aux visiteurs ayant mis le commerce en favoris. */
    @Builder.Default
    @Column(name = "notification_push", columnDefinition = "BOOLEAN DEFAULT false")
    private boolean notificationPush = false;

    /** Nom d'affichage du plan (ex: "Basique", "Premium", "Gold"). */
    @Column(name = "nom_affiche", length = 64)
    private String nomAffiche;

    /** Description marketing du plan. */
    @Column(name = "description_plan", columnDefinition = "TEXT")
    private String descriptionPlan;
}
