package com.carrefourconnect.testservices;

import com.carrefourconnect.dtos.MediaDTO;
import com.carrefourconnect.entities.Media;
import com.carrefourconnect.mappers.MediaMapper;
import com.carrefourconnect.repositories.MediaRepository;
import com.carrefourconnect.services.implementations.MediaServiceImpl;
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

class MediaServiceImplTest {

    @Mock
    private MediaRepository repository;
    @Mock
    private MediaMapper mapper;

    @InjectMocks
    private MediaServiceImpl service;

    private UUID id;
    private Media entity;
    private MediaDTO dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        id = UUID.randomUUID();
        entity = new Media();
        entity.setIdmedia(id);
        dto = new MediaDTO();
    }

    @Test
    void testFindById() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);
        assertNotNull(service.findById(id));
    }

    @Test
    void testFindByCommerce() {
        when(repository.findByCommerce_Idcommerce(any())).thenReturn(Collections.singletonList(entity));
        when(mapper.toDto(entity)).thenReturn(dto);
        assertFalse(service.findByCommerce(UUID.randomUUID()).isEmpty());
    }

    @Test
    void testSave() {
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);
        assertNotNull(service.save(dto));
    }
}
