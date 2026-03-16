package com.carrefourconnect.testcontrollers;

import com.carrefourconnect.controllers.LocalisationController;
import com.carrefourconnect.dtos.LocalisationDTO;
import com.carrefourconnect.services.interfaces.LocalisationService;
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

@WebMvcTest(LocalisationController.class)
class LocalisationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocalisationService service;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID id;
    private LocalisationDTO dto;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        dto = new LocalisationDTO();
        dto.setIdlocalisation(id);
    }

    @Test
    void testGetAll() throws Exception {
        when(service.findAll()).thenReturn(Collections.singletonList(dto));
        mockMvc.perform(get("/api/localisations"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetByVille() throws Exception {
        when(service.findByVille(any())).thenReturn(Collections.singletonList(dto));
        mockMvc.perform(get("/api/localisations/ville/Paris"))
                .andExpect(status().isOk());
    }
}
