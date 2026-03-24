package com.carrefourconnect.testservices;

import com.carrefourconnect.dtos.CommerceDTO;
import com.carrefourconnect.entities.Commerce;
import com.carrefourconnect.mappers.CommerceMapper;
import com.carrefourconnect.repositories.CommerceRepository;
import com.carrefourconnect.services.implementations.CommerceServiceImpl;
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

class CommerceServiceImplTest {

    @Mock
    private CommerceRepository repository;
    @Mock
    private CommerceMapper mapper;

    @InjectMocks
    private CommerceServiceImpl service;

    private UUID id;
    private Commerce entity;
    private CommerceDTO dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        id = UUID.randomUUID();
        entity = new Commerce();
        entity.setIdcommerce(id);
        dto = new CommerceDTO();
    }

    @Test
    void testFindById() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);
        assertNotNull(service.findById(id));
    }

    @Test
    void testSearchByName() {
        when(repository.findByNomContainingIgnoreCase(any())).thenReturn(Collections.singletonList(entity));
        when(mapper.toDto(entity)).thenReturn(dto);
        assertFalse(service.searchByName("test").isEmpty());
    }

    @Test
    void testFindNearby() {
        when(repository.findNearby(anyDouble(), anyDouble(), anyDouble())).thenReturn(Collections.singletonList(entity));
        when(mapper.toDto(entity)).thenReturn(dto);
        assertFalse(service.findNearby(0.0, 0.0, 10.0).isEmpty());
    }

    @Test
    void testSave() {
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);
        assertNotNull(service.save(dto));
    }
}
