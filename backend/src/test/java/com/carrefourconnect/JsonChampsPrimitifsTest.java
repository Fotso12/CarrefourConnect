package com.carrefourconnect;

import com.carrefourconnect.dtos.AbonnementDTO;
import com.carrefourconnect.dtos.NotificationDTO;
import com.carrefourconnect.services.interfaces.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Régression de la migration vers Jackson 3 : un JSON reçu sans un champ primitif
 * (boolean, int) doit donner la valeur par défaut, et non une erreur 400.
 * Utilise le mapper configuré par l'application (application.properties).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class JsonChampsPrimitifsTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void notificationSansLeChampLu() {
        NotificationDTO dto = objectMapper.readValue("{\"titre\":\"Bonjour\",\"message\":\"m\"}", NotificationDTO.class);

        assertThat(dto.getTitre()).isEqualTo("Bonjour");
        assertThat(dto.isLu()).isFalse();
    }

    @Test
    void abonnementAvecSeulementQuelquesChamps() {
        AbonnementDTO dto = objectMapper.readValue("{\"maxPhotos\":5}", AbonnementDTO.class);

        assertThat(dto.getMaxPhotos()).isEqualTo(5);
        assertThat(dto.isMiseEnAvant()).isFalse();
        assertThat(dto.getPrioriteAffichage()).isZero();
    }
}
