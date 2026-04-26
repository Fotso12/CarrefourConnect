package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.AvisDTO;
import com.carrefourconnect.entities.Avis;
import com.carrefourconnect.entities.Commerce;
import com.carrefourconnect.mappers.AvisMapper;
import com.carrefourconnect.repositories.AvisRepository;
import com.carrefourconnect.repositories.CommerceRepository;
import com.carrefourconnect.repositories.VisiteurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AvisServiceImplTest {

    @Mock
    private AvisRepository repository;
    @Mock
    private CommerceRepository commerceRepository;
    @Mock
    private VisiteurRepository visiteurRepository;
    @Mock
    private AvisMapper mapper;

    @InjectMocks
    private AvisServiceImpl service;

    private UUID id;
    private Avis entity;
    private AvisDTO dto;
    private Commerce commerce;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        id = UUID.randomUUID();
        commerce = new Commerce();
        commerce.setIdcommerce(UUID.randomUUID());
        commerce.setNoteGlobale(BigDecimal.ZERO);
        
        entity = new Avis();
        entity.setIdavis(id);
        entity.setCommerce(commerce);
        
        dto = new AvisDTO();
        dto.setIdcommerce(commerce.getIdcommerce());
    }

    @Test
    void testFindById() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);
        
        AvisDTO result = service.findById(id);
        
        assertNotNull(result);
        verify(repository).findById(id);
    }

    @Test
    void testSave() {
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(repository.calculateAverageRating(any())).thenReturn(BigDecimal.valueOf(4.5));
        when(commerceRepository.findById(any())).thenReturn(Optional.of(commerce));
        when(mapper.toDto(entity)).thenReturn(dto);

        AvisDTO result = service.save(dto);

        assertNotNull(result);
        verify(repository).save(entity);
        verify(commerceRepository).save(commerce);
        assertEquals(BigDecimal.valueOf(4.5), commerce.getNoteGlobale());
    }

    @Test
    void testDelete() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.calculateAverageRating(any())).thenReturn(BigDecimal.valueOf(4.0));
        when(commerceRepository.findById(any())).thenReturn(Optional.of(commerce));

        service.delete(id);
        
        verify(repository).deleteById(id);
        verify(commerceRepository).save(commerce);
        assertEquals(BigDecimal.valueOf(4.0), commerce.getNoteGlobale());
    }

    @Test
    void testFindByCommerce() {
        when(repository.findByCommerce_Idcommerce(any())).thenReturn(Collections.singletonList(entity));
        when(mapper.toDto(entity)).thenReturn(dto);
        
        assertFalse(service.findByCommerce(commerce.getIdcommerce()).isEmpty());
    }
}
