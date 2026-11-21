package com.carrefourconnect.config;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Régression : l'URL du dossier uploads doit être une URL "file:" LOCALE (sans nom
 * d'hôte), sinon les images renvoient une erreur 500 dans le conteneur Linux.
 */
class WebConfigTest {

    @Test
    void emplacementUploadsEstUneUrlFichierLocaleSurTousLesSystemes() {
        String location = WebConfig.uploadsLocation();
        URI uri = URI.create(location);

        assertThat(uri.getScheme()).isEqualTo("file");
        // file://app/uploads/ aurait "app" comme hôte : c'était le bug
        assertThat(uri.getHost()).isNull();
        assertThat(location).endsWith("/");
        assertThat(Paths.get(uri)).isEqualTo(Path.of("uploads").toAbsolutePath().normalize());
    }
}
