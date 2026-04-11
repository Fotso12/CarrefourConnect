package com.carrefourconnect.testservices;

import com.carrefourconnect.dtos.AvisDTO;
import com.carrefourconnect.entities.Avis;
import com.carrefourconnect.entities.Commerce;
import com.carrefourconnect.mappers.AvisMapper;
import com.carrefourconnect.repositories.AvisRepository;
import com.carrefourconnect.repositories.CommerceRepository;
import com.carrefourconnect.repositories.VisiteurRepository;
import com.carrefourconnect.services.implementations.AvisServiceImpl;
import com.carrefourconnect.utils.enums.StatutAvis;
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
        assertNotNull(service.findById(id));
    }

    @Test
    void testSave() {
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(repository.calculateAverageRating(any())).thenReturn(BigDecimal.valueOf(4.5));
        when(commerceRepository.findById(any())).thenReturn(Optional.of(commerce));
        when(mapper.toDto(entity)).thenReturn(dto);

        assertNotNull(service.save(dto));
        verify(repository).save(entity);
        verify(commerceRepository).save(any());
    }

    @Test
    void testDelete() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.calculateAverageRating(any())).thenReturn(BigDecimal.valueOf(4.0));
        when(commerceRepository.findById(any())).thenReturn(Optional.of(commerce));

        service.delete(id);
        
        verify(repository).deleteById(id);
        verify(commerceRepository).save(any());
    }

    @Test
    void testFindByCommerce() {
        when(repository.findByCommerce_Idcommerce(any())).thenReturn(Collections.singletonList(entity));
        assertFalse(service.findByCommerce(UUID.randomUUID()).isEmpty());
    }
}
