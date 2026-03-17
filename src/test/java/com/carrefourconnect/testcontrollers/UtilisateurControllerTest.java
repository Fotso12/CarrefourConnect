package com.carrefourconnect.testcontrollers;

import com.carrefourconnect.controllers.UtilisateurController;
import com.carrefourconnect.dtos.UtilisateurDTO;
import com.carrefourconnect.dtos.VisiteurDTO;
import com.carrefourconnect.services.interfaces.UtilisateurService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = UtilisateurController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class UtilisateurControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UtilisateurService service;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID id;
    private UtilisateurDTO dto;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        dto = new UtilisateurDTO();
        dto.setIduser(id);
    }

    @Test
    void testGetAll() throws Exception {
        when(service.findAll()).thenReturn(Collections.singletonList(dto));
        mockMvc.perform(get("/api/utilisateurs"))
                .andExpect(status().isOk());
    }

    @Test
    void testRegisterVisiteur() throws Exception {
        VisiteurDTO visiteurDto = new VisiteurDTO();
        when(service.registerVisiteur(any())).thenReturn(dto);
        mockMvc.perform(post("/api/utilisateurs/inscription/visiteur")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(visiteurDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void testAddFavorite() throws Exception {
        mockMvc.perform(post("/api/utilisateurs/{userId}/favoris/{commerceId}", id, UUID.randomUUID()))
                .andExpect(status().isOk());
    }
}
