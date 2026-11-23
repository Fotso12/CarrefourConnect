package com.carrefourconnect.config;

import com.carrefourconnect.entities.Abonnement;
import com.carrefourconnect.entities.Categorie;
import com.carrefourconnect.entities.Role;
import com.carrefourconnect.repositories.AbonnementRepository;
import com.carrefourconnect.repositories.CategorieRepository;
import com.carrefourconnect.repositories.RoleRepository;
import com.carrefourconnect.utils.enums.StatutAbonnement;
import com.carrefourconnect.utils.enums.TypeAbonnement;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Initialisation des données de base au démarrage de l'application.
 *
 * Plusieurs instances peuvent démarrer EN MÊME TEMPS (2 pods dans Kubernetes) : le schéma
 * "si absent alors insérer" n'est pas atomique, les deux instances peuvent voir "absent" et
 * insérer la même ligne. Les contraintes uniques (role.nom, categorie.nom,
 * abonnement.reference) garantissent qu'une seule insertion réussit ; l'autre instance
 * reçoit une DataIntegrityViolationException, qu'on ignore ici car la donnée existe bien.
 */
@Configuration
@Profile("!test")
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository,
                                     CategorieRepository categorieRepository,
                                     AbonnementRepository abonnementRepository,
                                     com.carrefourconnect.repositories.CommerceRepository commerceRepository) {
        return args -> {
            log.info("Vérification et initialisation des données de base...");

            // Rôles
            initRole(roleRepository, "ADMIN", "Administrateur de la plateforme");
            initRole(roleRepository, "COMMERCANT", "Partenaire commerçant");
            initRole(roleRepository, "VISITEUR", "Utilisateur standard / client");

            // Catégorie par défaut
            if (categorieRepository.count() == 0) {
                log.info("Création d'une catégorie par défaut...");
                Categorie cat = Categorie.builder()
                        .nom("Général")
                        .description("Catégorie par défaut pour tous les commerces")
                        .build();
                saveIgnoringConcurrentInsert(() -> categorieRepository.save(cat), "catégorie Général");
            }

            // Abonnements de référence (un par type) avec leurs droits
            initAbonnement(abonnementRepository,
                    TypeAbonnement.BASIQUE,
                    new BigDecimal("5000"),
                    3,       // maxPhotos
                    false,   // offreSpecialeAutorisee
                    false,   // miseEnAvant
                    1,       // prioriteAffichage
                    false,   // lienWhatsapp
                    false,   // notificationPush
                    "Basique",
                    "L'essentiel pour apparaître sur la carte.",
                    "REF-BASIQUE-FREE");

            initAbonnement(abonnementRepository,
                    TypeAbonnement.PREMIUM,
                    new BigDecimal("10000"),
                    10,      // maxPhotos
                    true,    // offreSpecialeAutorisee
                    true,    // miseEnAvant
                    2,       // prioriteAffichage
                    true,    // lienWhatsapp
                    false,   // notificationPush
                    "Premium",
                    "Le meilleur rapport visibilité / prix.",
                    "REF-PREMIUM-DEFAULT");

            initAbonnement(abonnementRepository,
                    TypeAbonnement.GOLD,
                    new BigDecimal("15000"),
                    -1,      // maxPhotos (-1 = illimité)
                    true,    // offreSpecialeAutorisee
                    true,    // miseEnAvant (VIP)
                    3,       // prioriteAffichage
                    true,    // lienWhatsapp
                    true,    // notificationPush
                    "Gold",
                    "Performance maximale pour les pros.",
                    "REF-GOLD-DEFAULT");

            // Migration : Lier les abonnements spécifiques existants à leur commerce
            log.info("Migration : Backfill idCommerce pour les abonnements existants...");
            commerceRepository.findAllWithAbonnement().forEach(c -> {
                if (c.getAbonnement() != null && c.getAbonnement().getIdCommerce() == null) {
                    Abonnement a = c.getAbonnement();
                    // On ne lie que si c'est une instance spécifique (pas une REF template)
                    if (a.getReference() != null && a.getReference().startsWith("SUB-")) {
                        a.setIdCommerce(c.getIdcommerce());
                        abonnementRepository.save(a);
                    }
                }
            });

            log.info("Initialisation des données terminée.");
        };
    }

    private void initRole(RoleRepository repository, String nom, String description) {
        if (repository.findByNom(nom).isEmpty()) {
            log.info("Création du rôle : {}", nom);
            Role role = Role.builder()
                    .nom(nom)
                    .description(description)
                    .build();
            saveIgnoringConcurrentInsert(() -> repository.save(role), "rôle " + nom);
        }
    }

    /**
     * Exécute une insertion de donnée de référence. Si une autre instance du backend vient
     * d'insérer la même ligne (violation de contrainte unique), on continue normalement :
     * l'objectif "la donnée existe" est atteint.
     */
    static void saveIgnoringConcurrentInsert(Runnable insertion, String libelle) {
        try {
            insertion.run();
        } catch (DataIntegrityViolationException e) {
            log.info("{} déjà créé(e) par une autre instance, on continue.", libelle);
        }
    }

    private void initAbonnement(AbonnementRepository repository,
                                TypeAbonnement type,
                                BigDecimal montant,
                                int maxPhotos,
                                boolean offreSpecialeAutorisee,
                                boolean miseEnAvant,
                                int prioriteAffichage,
                                boolean lienWhatsapp,
                                boolean notificationPush,
                                String nomAffiche,
                                String descriptionPlan,
                                String reference) {

        boolean exists = repository.findAll().stream().anyMatch(a -> type.equals(a.getType()));
        if (!exists) {
            log.info("Création de l'abonnement de référence : {}", type);
            Abonnement abo = Abonnement.builder()
                    .type(type)
                    .statut(StatutAbonnement.ACTIF)
                    .montant(montant)
                    .dateDebut(LocalDateTime.now())
                    .dateFin(LocalDateTime.now().plusMonths(99))
                    .reference(reference)
                    .maxPhotos(maxPhotos)
                    .offreSpecialeAutorisee(offreSpecialeAutorisee)
                    .miseEnAvant(miseEnAvant)
                    .prioriteAffichage(prioriteAffichage)
                    .lienWhatsapp(lienWhatsapp)
                    .notificationPush(notificationPush)
                    .nomAffiche(nomAffiche)
                    .descriptionPlan(descriptionPlan)
                    .build();
            saveIgnoringConcurrentInsert(() -> repository.save(abo), "abonnement de référence " + type);
        } else {
            // Mise à jour des champs de droits sur l'abonnement de référence existant
            // (au cas où l'abonnement existait avant l'ajout des nouveaux champs)
            repository.findByReference(reference).ifPresent(abo -> {
                if (abo.getNomAffiche() == null || abo.getNomAffiche().isEmpty()) {
                    abo.setNomAffiche(nomAffiche);
                    abo.setDescriptionPlan(descriptionPlan);
                    abo.setMaxPhotos(maxPhotos);
                    abo.setOffreSpecialeAutorisee(offreSpecialeAutorisee);
                    abo.setMiseEnAvant(miseEnAvant);
                    abo.setPrioriteAffichage(prioriteAffichage);
                    abo.setLienWhatsapp(lienWhatsapp);
                    abo.setNotificationPush(notificationPush);
                    repository.save(abo);
                    log.info("Mise à jour des droits de l'abonnement existant: {}", type);
                }
            });
        }
    }
}
