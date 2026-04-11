package com.carrefourconnect;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import com.carrefourconnect.services.interfaces.NotificationService;
import com.carrefourconnect.services.implementations.EmailService;
import com.carrefourconnect.repositories.*;
import javax.sql.DataSource;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de démarrage du contexte applicatif.
 * Utilise le profil 'test' qui configure une base H2 en mémoire.
 */
@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.NONE,
	properties = {
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration",
		"spring.main.allow-bean-definition-overriding=true"
	}
)
@ActiveProfiles("test")
class CarrefourconnectApplicationTests {

	@MockBean
	private JavaMailSender javaMailSender;

	@MockBean
	private NotificationService notificationService;

	@MockBean
	private EmailService emailService;

	@MockBean
	private DataSource dataSource;

	@MockBean
	private CommerceRepository commerceRepository;

	@MockBean
	private CategorieRepository categorieRepository;

	@MockBean
	private VisiteurRepository visiteurRepository;

	@MockBean
	private CommercantRepository commercantRepository;

	@MockBean
	private AbonnementRepository abonnementRepository;

	@MockBean
	private RoleRepository roleRepository;

	@Test
	void contextLoads() {
		// Vérifie que le contexte Spring démarre sans erreur
	}

}
