package com.carrefourconnect.testcontrollers;

import com.carrefourconnect.controllers.AvisController;
import com.carrefourconnect.dtos.AvisDTO;
import com.carrefourconnect.services.interfaces.AvisService;
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
    controllers = AvisController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class AvisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvisService service;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID id;
    private AvisDTO dto;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        dto = new AvisDTO();
        dto.setIdavis(id);
    }

    @Test
    void testGetAll() throws Exception {
        when(service.findAll()).thenReturn(Collections.singletonList(dto));
        mockMvc.perform(get("/api/avis"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetByCommerce() throws Exception {
        when(service.findByCommerce(any())).thenReturn(Collections.singletonList(dto));
        mockMvc.perform(get("/api/avis/commerce/{commerceId}", UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    void testSave() throws Exception {
        when(service.save(any())).thenReturn(dto);
        mockMvc.perform(post("/api/avis")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }
}
