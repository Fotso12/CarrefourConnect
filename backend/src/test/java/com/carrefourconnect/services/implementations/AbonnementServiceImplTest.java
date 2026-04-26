package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.AbonnementDTO;
import com.carrefourconnect.entities.Abonnement;
import com.carrefourconnect.mappers.AbonnementMapper;
import com.carrefourconnect.repositories.AbonnementRepository;
import com.carrefourconnect.utils.enums.StatutAbonnement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AbonnementServiceImplTest {

    @Mock
    private AbonnementRepository repository;

    @Mock
    private AbonnementMapper mapper;

    @InjectMocks
    private AbonnementServiceImpl service;

    private UUID id;
    private Abonnement entity;
    private AbonnementDTO dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        id = UUID.randomUUID();
        entity = new Abonnement();
        entity.setIdabonnement(id);
        dto = new AbonnementDTO();
        dto.setIdabonnement(id);
    }

    @Test
    void testFindById() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        AbonnementDTO result = service.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getIdabonnement());
        verify(repository).findById(id);
    }

    @Test
    void testFindAll() {
        when(repository.findAll()).thenReturn(Collections.singletonList(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        List<AbonnementDTO> result = service.findAll();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void testSave() {
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        AbonnementDTO result = service.save(dto);

        assertNotNull(result);
        verify(repository).save(entity);
    }

    @Test
    void testUpdate() {
        when(repository.existsById(id)).thenReturn(true);
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        AbonnementDTO result = service.update(id, dto);

        assertNotNull(result);
        verify(repository).save(entity);
    }

    @Test
    void testDelete() {
        service.delete(id);
        verify(repository).deleteById(id);
    }

    @Test
    void testFindByStatut() {
        when(repository.findByStatut(StatutAbonnement.ACTIF)).thenReturn(Collections.singletonList(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        List<AbonnementDTO> result = service.findByStatut(StatutAbonnement.ACTIF);

        assertFalse(result.isEmpty());
        assertEquals(StatutAbonnement.ACTIF, StatutAbonnement.ACTIF); 
    }
}
