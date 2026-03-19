package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.CommerceDTO;
import com.carrefourconnect.services.interfaces.CommerceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = CommerceController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class CommerceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommerceService service;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID id;
    private CommerceDTO dto;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        dto = new CommerceDTO();
        dto.setIdcommerce(id);
        dto.setNom("Test Commerce");
    }

    @Test
    void testGetAll() throws Exception {
        when(service.findAll()).thenReturn(Collections.singletonList(dto));
        mockMvc.perform(get("/api/commerces"))
                .andExpect(status().isOk());
    }

    @Test
    void testFindNearby() throws Exception {
        when(service.findNearby(anyDouble(), anyDouble(), anyDouble())).thenReturn(Collections.singletonList(dto));
        mockMvc.perform(get("/api/commerces/proximite")
                .param("lat", "45.0")
                .param("lon", "5.0")
                .param("distance", "10.0"))
                .andExpect(status().isOk());
    }
}
