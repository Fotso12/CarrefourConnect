package com.carrefourconnect;

import com.carrefourconnect.services.interfaces.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie les endpoints Actuator utilisés par Kubernetes (sondes) et Prometheus.
 * Ils doivent répondre SANS jeton JWT, et les autres endpoints Actuator ne doivent pas être exposés.
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMetrics // les tests désactivent l'export des métriques par défaut
@ActiveProfiles("test")
class ActuatorEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PathMappedEndpoints endpoints;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void sondeLivenessAccessibleSansAuthentification() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void sondeReadinessAccessibleSansAuthentification() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void metriquesPrometheusExposees() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("jvm_memory_used_bytes")));
    }

    @Test
    void seulsHealthEtPrometheusSontExposes() {
        // Une requête HTTP sur /actuator/env renverrait 401 de toute façon (Spring Security) :
        // on vérifie donc directement la liste des endpoints publiés par Actuator.
        assertThat(endpoints.getAllRootPaths())
                .containsExactlyInAnyOrder("health", "prometheus");
    }
}
