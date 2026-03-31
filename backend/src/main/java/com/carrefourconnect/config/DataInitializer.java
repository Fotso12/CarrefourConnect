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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Initialisation des données de base au démarrage de l'application.
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository, 
                                     CategorieRepository categorieRepository,
                                     AbonnementRepository abonnementRepository) {
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
                categorieRepository.save(cat);
            }

            // Abonnements de référence (un par type)
            initAbonnement(abonnementRepository, TypeAbonnement.BASIQUE,  new BigDecimal("0"),     "REF-BASIQUE-FREE");
            initAbonnement(abonnementRepository, TypeAbonnement.PREMIUM,  new BigDecimal("5000"),  "REF-PREMIUM-DEFAULT");
            initAbonnement(abonnementRepository, TypeAbonnement.GOLD,     new BigDecimal("10000"), "REF-GOLD-DEFAULT");

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
            repository.save(role);
        }
    }

    private void initAbonnement(AbonnementRepository repository, TypeAbonnement type, BigDecimal montant, String reference) {
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
                    .build();
            repository.save(abo);
        }
    }
}
