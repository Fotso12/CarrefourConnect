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

            // Abonnement par défaut
            if (abonnementRepository.count() == 0) {
                log.info("Création d'un abonnement par défaut...");
                Abonnement abo = Abonnement.builder()
                        .type(TypeAbonnement.BASIQUE)
                        .statut(StatutAbonnement.ACTIF)
                        .montant(BigDecimal.ZERO)
                        .dateDebut(LocalDateTime.now())
                        .dateFin(LocalDateTime.now().plusYears(1))
                        .reference("REF-BASIQUE-FREE")
                        .build();
                abonnementRepository.save(abo);
            }

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
}
