package com.carrefourconnect.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${app.firebase.config-path:}")
    private String configPath;

    @PostConstruct
    public void initialize() {
        try {
            if (configPath == null || configPath.isEmpty()) {
                log.warn("Chemin de configuration Firebase non défini (app.firebase.config-path). Les notifications push ne seront pas envoyées.");
                return;
            }

            FileInputStream serviceAccount = new FileInputStream(configPath);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase a été initialisé avec succès.");
            }
        } catch (IOException e) {
            log.error("Erreur lors de l'initialisation de Firebase: {}", e.getMessage());
        }
    }
}
