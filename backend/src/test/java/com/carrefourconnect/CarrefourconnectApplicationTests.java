package com.carrefourconnect;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import com.carrefourconnect.services.interfaces.NotificationService;
import com.carrefourconnect.services.implementations.EmailService;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de démarrage du contexte applicatif.
 * Utilise le profil 'test' qui configure une base H2 en mémoire.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class CarrefourconnectApplicationTests {

	@MockBean
	private JavaMailSender javaMailSender;

	@MockBean
	private NotificationService notificationService;

	@MockBean
	private EmailService emailService;

	@Test
	void contextLoads() {
		// Vérifie que le contexte Spring démarre sans erreur
	}

}
