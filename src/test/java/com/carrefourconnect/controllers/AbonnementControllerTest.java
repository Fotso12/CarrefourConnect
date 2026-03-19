package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.AbonnementDTO;
import com.carrefourconnect.services.interfaces.AbonnementService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = AbonnementController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class AbonnementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AbonnementService service;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID id;
    private AbonnementDTO dto;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        dto = new AbonnementDTO();
    }

    @Test
    void testGetAll() throws Exception {
        when(service.findAll()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/abonnements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetById() throws Exception {
        when(service.findById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/abonnements/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    void testGetByIdNotFound() throws Exception {
        when(service.findById(id)).thenReturn(null);

        mockMvc.perform(get("/api/abonnements/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSave() throws Exception {
        when(service.save(any(AbonnementDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/api/abonnements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void testUpdate() throws Exception {
        when(service.update(eq(id), any(AbonnementDTO.class))).thenReturn(dto);

        mockMvc.perform(put("/api/abonnements/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/abonnements/{id}", id))
                .andExpect(status().isNoContent());
    }
}
