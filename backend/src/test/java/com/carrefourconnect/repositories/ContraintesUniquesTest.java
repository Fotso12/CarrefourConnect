package com.carrefourconnect.repositories;

import com.carrefourconnect.entities.Categorie;
import com.carrefourconnect.entities.Role;
import com.carrefourconnect.services.interfaces.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Vérifie que la base refuse vraiment les doublons de données de référence :
 * c'est ce qui protège l'initialisation quand plusieurs pods démarrent ensemble.
 */
@SpringBootTest
@ActiveProfiles("test")
class ContraintesUniquesTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CategorieRepository categorieRepository;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void deuxRolesDeMemeNomSontRefuses() {
        roleRepository.saveAndFlush(Role.builder().nom("ROLE_DOUBLON_TEST").description("1").build());

        assertThrows(DataIntegrityViolationException.class, () ->
                roleRepository.saveAndFlush(Role.builder().nom("ROLE_DOUBLON_TEST").description("2").build()));
    }

    @Test
    void deuxCategoriesDeMemeNomSontRefusees() {
        categorieRepository.saveAndFlush(Categorie.builder().nom("Catégorie doublon test").build());

        assertThrows(DataIntegrityViolationException.class, () ->
                categorieRepository.saveAndFlush(Categorie.builder().nom("Catégorie doublon test").build()));
    }
}
