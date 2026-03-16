package com.carrefourconnect.testservices;

import com.carrefourconnect.dtos.OffreDTO;
import com.carrefourconnect.entities.Offre;
import com.carrefourconnect.mappers.OffreMapper;
import com.carrefourconnect.repositories.OffreRepository;
import com.carrefourconnect.services.implementations.OffreServiceImpl;
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

class OffreServiceImplTest {

    @Mock
    private OffreRepository repository;
    @Mock
    private OffreMapper mapper;

    @InjectMocks
    private OffreServiceImpl service;

    private UUID id;
    private Offre entity;
    private OffreDTO dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        id = UUID.randomUUID();
        entity = new Offre();
        entity.setIdoffre(id);
        dto = new OffreDTO();
    }

    @Test
    void testFindById() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);
        assertNotNull(service.findById(id));
    }

    @Test
    void testFindActiveOffres() {
        when(repository.findActiveOffres()).thenReturn(Collections.singletonList(entity));
        when(mapper.toDto(entity)).thenReturn(dto);
        assertFalse(service.findActiveOffres().isEmpty());
    }

    @Test
    void testSave() {
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);
        assertNotNull(service.save(dto));
    }
}
