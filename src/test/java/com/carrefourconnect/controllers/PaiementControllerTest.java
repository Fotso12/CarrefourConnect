package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.PaiementDTO;
import com.carrefourconnect.services.interfaces.PaiementService;
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
    controllers = PaiementController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class PaiementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaiementService service;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID id;
    private PaiementDTO dto;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        dto = new PaiementDTO();
    }

    @Test
    void testGetAll() throws Exception {
        when(service.findAll()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/paiements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetById() throws Exception {
        when(service.findById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/paiements/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    void testSave() throws Exception {
        when(service.save(any(PaiementDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/api/paiements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void testGetByAbonnement() throws Exception {
        when(service.findByAbonnement(id)).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/paiements/abonnement/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    void testGetByReference() throws Exception {
        when(service.findByReference("REF123")).thenReturn(dto);

        mockMvc.perform(get("/api/paiements/reference/REF123"))
                .andExpect(status().isOk());
    }
}
