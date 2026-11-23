package com.carrefourconnect.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration pour servir le dossier uploads comme ressource statique.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadsLocation());
    }

    /**
     * URL "file:" du dossier uploads, valide sur tous les systèmes.
     * Path.toUri() produit file:///C:/.../uploads/ sous Windows et file:///app/uploads/
     * sous Linux. L'ancienne concaténation "file:/" + chemin donnait file://app/uploads/
     * dans le conteneur Docker : "app" y était lu comme un nom de serveur distant
     * (MalformedURLException "Unsupported non-local file URL", donc erreur 500).
     */
    static String uploadsLocation() {
        Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
        String location = uploadDir.toUri().toString();
        // Spring exige un "/" final pour traiter l'emplacement comme un dossier
        return location.endsWith("/") ? location : location + "/";
    }
}
