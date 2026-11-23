package com.carrefourconnect.controllers;

import com.carrefourconnect.entities.Role;
import com.carrefourconnect.entities.Utilisateur;
import com.carrefourconnect.repositories.RoleRepository;
import com.carrefourconnect.repositories.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sécurité des routes /api/notifications :
 * - envoi réservé aux administrateurs ;
 * - lecture et marquage "lu" réservés au propriétaire (ou à un administrateur) ;
 * - plus rien n'est accessible sans être connecté.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationSecuriteTest {

    private static final String MOT_DE_PASSE = "MotDePasse-Test1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @MockitoBean
    private JavaMailSender javaMailSender;

    private Connecte alice;
    private Connecte bob;
    private Connecte admin;

    private record Connecte(String id, String bearer) { }

    @BeforeEach
    void preparer() throws Exception {
        role("VISITEUR");
        role("ADMIN");
        alice = inscrireEtConnecter("alice.notif@example.com", false);
        bob = inscrireEtConnecter("bob.notif@example.com", false);
        admin = inscrireEtConnecter("admin.notif@example.com", true);
    }

    @Test
    void envoiInterditSansConnexion() throws Exception {
        mockMvc.perform(post("/api/notifications/send").contentType(MediaType.APPLICATION_JSON)
                        .content(notificationPour(bob.id())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void envoiInterditAUnVisiteur() throws Exception {
        mockMvc.perform(post("/api/notifications/send").header("Authorization", alice.bearer())
                        .contentType(MediaType.APPLICATION_JSON).content(notificationPour(bob.id())))
                .andExpect(status().isForbidden());
    }

    @Test
    void envoiAutoriseAUnAdministrateur() throws Exception {
        mockMvc.perform(post("/api/notifications/send").header("Authorization", admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON).content(notificationPour(bob.id())))
                .andExpect(status().isOk());
    }

    @Test
    void chacunNeLitQueSesPropresNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications/user/" + alice.id()).header("Authorization", alice.bearer()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/notifications/user/" + alice.id() + "/unread/count").header("Authorization", alice.bearer()))
                .andExpect(status().isOk());

        // Alice change l'identifiant dans l'URL pour lire les notifications de Bob : refusé
        mockMvc.perform(get("/api/notifications/user/" + bob.id()).header("Authorization", alice.bearer()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/notifications/user/" + bob.id() + "/unread/count").header("Authorization", alice.bearer()))
                .andExpect(status().isForbidden());

        // Sans connexion : refusé
        mockMvc.perform(get("/api/notifications/user/" + bob.id()))
                .andExpect(status().isUnauthorized());

        // Un administrateur peut consulter celles de n'importe qui
        mockMvc.perform(get("/api/notifications/user/" + bob.id()).header("Authorization", admin.bearer()))
                .andExpect(status().isOk());
    }

    @Test
    void seulLeDestinataireMarqueSaNotificationCommeLue() throws Exception {
        String reponse = mockMvc.perform(post("/api/notifications/send").header("Authorization", admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON).content(notificationPour(bob.id())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String idNotification = objectMapper.readTree(reponse).get("idnotification").asString();

        mockMvc.perform(put("/api/notifications/" + idNotification + "/lu").header("Authorization", alice.bearer()))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/notifications/" + idNotification + "/lu").header("Authorization", bob.bearer()))
                .andExpect(status().isOk());
    }

    // ---------- outils ----------

    private void role(String nom) {
        if (roleRepository.findByNom(nom).isEmpty()) {
            roleRepository.save(Role.builder().nom(nom).description(nom).build());
        }
    }

    private Connecte inscrireEtConnecter(String email, boolean administrateur) throws Exception {
        if (utilisateurRepository.findByEmail(email).isEmpty()) {
            mockMvc.perform(post("/api/utilisateurs/inscription/visiteur").contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"nom":"Test","prenom":"Notif","email":"%s","telephone":"690000009","password":"%s"}"""
                                    .formatted(email, MOT_DE_PASSE)))
                    .andExpect(status().isCreated());
            if (administrateur) {
                Utilisateur u = utilisateurRepository.findByEmail(email).orElseThrow();
                u.setRole(roleRepository.findByNom("ADMIN").orElseThrow());
                utilisateurRepository.save(u);
            }
        }
        String reponse = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode jwt = objectMapper.readTree(reponse);
        return new Connecte(jwt.get("id").asString(), "Bearer " + jwt.get("token").asString());
    }

    private static String notificationPour(String iduser) {
        return "{\"iduser\":\"%s\",\"titre\":\"Test\",\"message\":\"Message\",\"type\":\"INFO\"}".formatted(iduser);
    }
}
