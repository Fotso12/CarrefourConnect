package com.carrefourconnect.testcontrollers;

import com.carrefourconnect.controllers.OffreController;
import com.carrefourconnect.dtos.OffreDTO;
import com.carrefourconnect.services.interfaces.OffreService;
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

@WebMvcTest(OffreController.class)
class OffreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OffreService service;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID id;
    private OffreDTO dto;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        dto = new OffreDTO();
        dto.setIdoffre(id);
    }

    @Test
    void testGetAll() throws Exception {
        when(service.findAll()).thenReturn(Collections.singletonList(dto));
        mockMvc.perform(get("/api/offres"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetActiveOffres() throws Exception {
        when(service.findActiveOffres()).thenReturn(Collections.singletonList(dto));
        mockMvc.perform(get("/api/offres/active"))
                .andExpect(status().isOk());
    }
}
