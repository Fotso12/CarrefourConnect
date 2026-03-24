package com.carrefourconnect.config;

import com.carrefourconnect.entities.Role;
import com.carrefourconnect.repositories.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initialisation des données de base au démarrage de l'application.
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            log.info("Vérification et initialisation des rôles...");

            initRole(roleRepository, "ADMIN", "Administrateur de la plateforme");
            initRole(roleRepository, "COMMERCANT", "Partenaire commerçant");
            initRole(roleRepository, "VISITEUR", "Utilisateur standard / client");

            log.info("Initialisation des rôles terminée.");
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
