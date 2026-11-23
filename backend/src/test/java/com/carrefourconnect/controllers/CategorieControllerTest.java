package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.CategorieDTO;
import com.carrefourconnect.services.interfaces.CategorieService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = CategorieController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class CategorieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategorieService service;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID id;
    private CategorieDTO dto;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        dto = new CategorieDTO();
        dto.setIdcategorie(id);
        dto.setNom("Test Category");
    }

    @Test
    void testGetAll() throws Exception {
        when(service.findAll()).thenReturn(Collections.singletonList(dto));
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk());
    }

    @Test
    void testSearchByName() throws Exception {
        when(service.findByNom(any())).thenReturn(dto);
        mockMvc.perform(get("/api/categories/recherche").param("nom", "test"))
                .andExpect(status().isOk());
    }
}
