package com.carrefourconnect.testservices;

import com.carrefourconnect.dtos.UtilisateurDTO;
import com.carrefourconnect.dtos.VisiteurDTO;
import com.carrefourconnect.entities.Commerce;
import com.carrefourconnect.entities.Utilisateur;
import com.carrefourconnect.entities.Visiteur;
import com.carrefourconnect.mappers.CommercantMapper;
import com.carrefourconnect.mappers.UtilisateurMapper;
import com.carrefourconnect.mappers.VisiteurMapper;
import com.carrefourconnect.repositories.CommerceRepository;
import com.carrefourconnect.repositories.CommercantRepository;
import com.carrefourconnect.repositories.UtilisateurRepository;
import com.carrefourconnect.repositories.VisiteurRepository;
import com.carrefourconnect.services.implementations.UtilisateurServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UtilisateurServiceImplTest {

    @Mock
    private UtilisateurRepository repository;
    @Mock
    private VisiteurRepository visiteurRepository;
    @Mock
    private CommercantRepository commercantRepository;
    @Mock
    private CommerceRepository commerceRepository;
    @Mock
    private UtilisateurMapper mapper;
    @Mock
    private VisiteurMapper visiteurMapper;
    @Mock
    private CommercantMapper commercantMapper;

    @InjectMocks
    private UtilisateurServiceImpl service;

    private UUID id;
    private Utilisateur entity;
    private UtilisateurDTO dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        id = UUID.randomUUID();
        entity = new Visiteur(); // Visiteur is concrete
        entity.setIduser(id);
        entity.setFavoris(new HashSet<>());
        dto = new UtilisateurDTO();
    }

    @Test
    void testFindById() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);
        assertNotNull(service.findById(id));
    }

    @Test
    void testAddFavorite() {
        Commerce commerce = new Commerce();
        commerce.setIdcommerce(UUID.randomUUID());
        
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(commerceRepository.findById(any())).thenReturn(Optional.of(commerce));
        
        service.addFavorite(id, commerce.getIdcommerce());
        
        verify(repository).save(entity);
        assertTrue(entity.getFavoris().contains(commerce));
    }

    @Test
    void testRegisterVisiteur() {
        VisiteurDTO visiteurDto = new VisiteurDTO();
        Visiteur visiteurEntity = new Visiteur();
        
        when(visiteurMapper.toEntity(any())).thenReturn(visiteurEntity);
        when(visiteurRepository.save(any())).thenReturn(visiteurEntity);
        when(visiteurMapper.toDto(any())).thenReturn(new VisiteurDTO());
        
        assertNotNull(service.registerVisiteur(visiteurDto));
    }

    @Test
    void testRegisterCommercant() {
        CommercantDTO commercantDto = new CommercantDTO();
        commercantDto.setEmail("test@ex.com");
        commercantDto.setNumeroRegistreCommerce("RC123");

        com.carrefourconnect.entities.Commercant entity = new com.carrefourconnect.entities.Commercant();
        
        when(commercantMapper.toEntity(any())).thenReturn(entity);
        when(commercantRepository.save(any())).thenReturn(entity);
        when(commercantMapper.toDto(any())).thenReturn(new CommercantDTO());
        
        assertNotNull(service.registerCommercant(commercantDto));
    }
}
