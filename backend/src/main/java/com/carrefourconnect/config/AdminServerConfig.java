package com.carrefourconnect.config;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration dédiée au serveur d'administration Spring Boot Admin.
 * Déplacée ici pour isoler l'annotation du contexte de démarrage principal
 * et faciliter les tests.
 */
@Configuration
@EnableAdminServer
public class AdminServerConfig {
}
