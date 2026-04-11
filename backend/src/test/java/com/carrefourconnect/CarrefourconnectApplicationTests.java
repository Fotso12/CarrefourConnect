package com.carrefourconnect;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de démarrage du contexte applicatif.
 * Utilise le profil 'test' qui configure une base H2 en mémoire.
 */
@SpringBootTest
@ActiveProfiles("test")
class CarrefourconnectApplicationTests {

	@Test
	void contextLoads() {
		// Vérifie que le contexte Spring démarre sans erreur
	}

}
