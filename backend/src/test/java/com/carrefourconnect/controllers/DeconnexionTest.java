package com.carrefourconnect.controllers;

import com.carrefourconnect.entities.Role;
import com.carrefourconnect.repositories.RoleRepository;
import com.carrefourconnect.services.interfaces.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Parcours complet : inscription, connexion, route protégée accessible, déconnexion,
 * puis le MÊME jeton est refusé (alors qu'il n'est pas encore expiré).
 * Ici avec la liste noire en mémoire ; RedisIntegrationTest couvre la version Redis.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DeconnexionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void apresDeconnexionLeJetonEstRefuse() throws Exception {
        if (roleRepository.findByNom("VISITEUR").isEmpty()) {
            roleRepository.save(Role.builder().nom("VISITEUR").description("Visiteur").build());
        }
        String email = "deconnexion.test@example.com";
        mockMvc.perform(post("/api/utilisateurs/inscription/visiteur")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Test","prenom":"Deconnexion","email":"%s",
                                 "telephone":"690000002","password":"MotDePasse-Test1"}""".formatted(email)))
                .andExpect(status().isCreated());

        String reponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"MotDePasse-Test1\"}".formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode jwt = objectMapper.readTree(reponse);
        String bearer = "Bearer " + jwt.get("token").asString();
        String routeProtegee = "/api/utilisateurs/" + jwt.get("id").asString() + "/favoris";

        mockMvc.perform(get(routeProtegee).header("Authorization", bearer))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/logout").header("Authorization", bearer))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(routeProtegee).header("Authorization", bearer))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deconnexionSansJetonRepond204() throws Exception {
        mockMvc.perform(post("/api/auth/logout")).andExpect(status().isNoContent());
    }
}
