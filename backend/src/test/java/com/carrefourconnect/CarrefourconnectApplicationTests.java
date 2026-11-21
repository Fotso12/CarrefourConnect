package com.carrefourconnect;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mail.javamail.JavaMailSender;
import com.carrefourconnect.services.interfaces.NotificationService;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

/**
 * Test de démarrage du contexte applicatif.
 * Utilise le profil 'test' qui configure une base H2 en mémoire.
 */
// MOCK (et non NONE) : la configuration Spring Security de l'application est une
// configuration web (servlet), elle a besoin d'un contexte web pour démarrer.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@DisabledIfEnvironmentVariable(named = "GITHUB_ACTIONS", matches = "true")
class CarrefourconnectApplicationTests {

	@MockitoBean
	private JavaMailSender javaMailSender;

	@MockitoBean
	private NotificationService notificationService;

	@Test
	void contextLoads() {
		// Vérifie que le contexte Spring démarre sans erreur
	}

}
