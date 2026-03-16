package com.carrefourconnect.testservices;

import com.carrefourconnect.dtos.LocalisationDTO;
import com.carrefourconnect.entities.Localisation;
import com.carrefourconnect.mappers.LocalisationMapper;
import com.carrefourconnect.repositories.LocalisationRepository;
import com.carrefourconnect.services.implementations.LocalisationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LocalisationServiceImplTest {

    @Mock
    private LocalisationRepository repository;
    @Mock
    private LocalisationMapper mapper;

    @InjectMocks
    private LocalisationServiceImpl service;

    private UUID id;
    private Localisation entity;
    private LocalisationDTO dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        id = UUID.randomUUID();
        entity = new Localisation();
        entity.setIdlocalisation(id);
        dto = new LocalisationDTO();
    }

    @Test
    void testFindById() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);
        assertNotNull(service.findById(id));
    }

    @Test
    void testFindByVille() {
        when(repository.findByVilleIgnoreCase(any())).thenReturn(Collections.singletonList(entity));
        when(mapper.toDto(entity)).thenReturn(dto);
        assertFalse(service.findByVille("Paris").isEmpty());
    }

    @Test
    void testSave() {
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);
        assertNotNull(service.save(dto));
    }
}
