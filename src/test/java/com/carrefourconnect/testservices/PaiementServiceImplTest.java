package com.carrefourconnect.testservices;

import com.carrefourconnect.dtos.PaiementDTO;
import com.carrefourconnect.entities.Paiement;
import com.carrefourconnect.mappers.PaiementMapper;
import com.carrefourconnect.repositories.PaiementRepository;
import com.carrefourconnect.services.implementations.PaiementServiceImpl;
import com.carrefourconnect.utils.enums.StatutPaiement;
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

class PaiementServiceImplTest {

    @Mock
    private PaiementRepository repository;

    @Mock
    private PaiementMapper mapper;

    @InjectMocks
    private PaiementServiceImpl service;

    private UUID id;
    private Paiement entity;
    private PaiementDTO dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        id = UUID.randomUUID();
        entity = new Paiement();
        entity.setIdpaiement(id);
        dto = new PaiementDTO();
    }

    @Test
    void testFindById() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        PaiementDTO result = service.findById(id);

        assertNotNull(result);
        verify(repository).findById(id);
    }

    @Test
    void testFindAll() {
        when(repository.findAll()).thenReturn(Collections.singletonList(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        List<PaiementDTO> result = service.findAll();

        assertFalse(result.isEmpty());
    }

    @Test
    void testSave() {
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        PaiementDTO result = service.save(dto);

        assertNotNull(result);
        verify(repository).save(entity);
    }

    @Test
    void testUpdate() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(any(Paiement.class))).thenReturn(entity);
        when(mapper.toDto(any(Paiement.class))).thenReturn(dto);

        PaiementDTO result = service.update(id, dto);

        assertNotNull(result);
    }

    @Test
    void testDelete() {
        doNothing().when(repository).deleteById(id);
        service.delete(id);
        verify(repository).deleteById(id);
    }

    @Test
    void testFindByReference() {
        when(repository.findByReference("REF123")).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        PaiementDTO result = service.findByReference("REF123");

        assertNotNull(result);
    }

    @Test
    void testFindByStatut() {
        when(repository.findByStatut(StatutPaiement.VALIDE)).thenReturn(Collections.singletonList(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        List<PaiementDTO> result = service.findByStatut(StatutPaiement.VALIDE);

        assertFalse(result.isEmpty());
    }
}
