package com.carrefourconnect.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO léger utilisé par l'admin pour mettre à jour la configuration
 * (droits et prix) d'un plan d'abonnement, sans toucher aux dates ni aux statuts.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanConfigDTO {

    /** Prix du plan en FCFA. */
    private BigDecimal montant;

    /** Nombre maximum de photos autorisées. -1 = illimité. */
    private int maxPhotos;

    /** Autorisation de créer des offres spéciales. */
    private boolean offreSpecialeAutorisee;

    /** Mise en avant du commerce dans les résultats. */
    private boolean miseEnAvant;

    /**
     * Priorité d'affichage.
     * 1 = Basique (bas), 2 = Premium (normal), 3 = Gold (VIP)
     */
    private int prioriteAffichage;

    /** Accès au lien WhatsApp direct. */
    private boolean lienWhatsapp;

    /** Envoi de notifications push aux favoris. */
    private boolean notificationPush;

    /** Nom d'affichage du plan. */
    private String nomAffiche;

    /** Description marketing du plan. */
    private String descriptionPlan;
}
