package com.carrefourconnect.testcontrollers;

import com.carrefourconnect.controllers.CategorieController;
import com.carrefourconnect.dtos.CategorieDTO;
import com.carrefourconnect.services.interfaces.CategorieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategorieController.class)
class CategorieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
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
