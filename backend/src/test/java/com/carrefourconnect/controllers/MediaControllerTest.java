package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.MediaDTO;
import com.carrefourconnect.services.interfaces.MediaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = MediaController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MediaService service;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID id;
    private MediaDTO dto;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        dto = new MediaDTO();
        dto.setIdmedia(id);
    }

    @Test
    void testGetAll() throws Exception {
        when(service.findAll()).thenReturn(Collections.singletonList(dto));
        mockMvc.perform(get("/api/medias"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetByCommerce() throws Exception {
        when(service.findByCommerce(any())).thenReturn(Collections.singletonList(dto));
        mockMvc.perform(get("/api/medias/commerce/{commerceId}", UUID.randomUUID()))
                .andExpect(status().isOk());
    }
}
