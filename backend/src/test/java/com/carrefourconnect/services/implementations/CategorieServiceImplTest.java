package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.CategorieDTO;
import com.carrefourconnect.entities.Categorie;
import com.carrefourconnect.mappers.CategorieMapper;
import com.carrefourconnect.repositories.CategorieRepository;
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

class CategorieServiceImplTest {

    @Mock
    private CategorieRepository repository;
    @Mock
    private CategorieMapper mapper;

    @InjectMocks
    private CategorieServiceImpl service;

    private UUID id;
    private Categorie entity;
    private CategorieDTO dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        id = UUID.randomUUID();
        entity = new Categorie();
        entity.setIdcategorie(id);
        
        dto = new CategorieDTO();
        dto.setIdcategorie(id);
    }

    @Test
    void testFindById() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);
        
        CategorieDTO result = service.findById(id);
        
        assertNotNull(result);
        assertEquals(id, result.getIdcategorie());
    }

    @Test
    void testFindAll() {
        when(repository.findAll()).thenReturn(Collections.singletonList(entity));
        when(mapper.toDto(entity)).thenReturn(dto);
        
        assertFalse(service.findAll().isEmpty());
    }

    @Test
    void testSave() {
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);
        
        assertNotNull(service.save(dto));
        verify(repository).save(entity);
    }

    @Test
    void testUpdate() {
        when(repository.existsById(id)).thenReturn(true);
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(any())).thenReturn(entity);
        when(mapper.toDto(any())).thenReturn(dto);
        
        CategorieDTO result = service.update(id, dto);
        
        assertNotNull(result);
        verify(repository).save(any());
    }

    @Test
    void testDelete() {
        service.delete(id);
        verify(repository).deleteById(id);
    }
}
