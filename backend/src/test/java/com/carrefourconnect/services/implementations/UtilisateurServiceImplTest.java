package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.CommercantDTO;
import com.carrefourconnect.dtos.UtilisateurDTO;
import com.carrefourconnect.dtos.VisiteurDTO;
import com.carrefourconnect.entities.Commerce;
import com.carrefourconnect.entities.Role;
import com.carrefourconnect.entities.Utilisateur;
import com.carrefourconnect.entities.Visiteur;
import com.carrefourconnect.mappers.CommercantMapper;
import com.carrefourconnect.mappers.UtilisateurMapper;
import com.carrefourconnect.mappers.VisiteurMapper;
import com.carrefourconnect.repositories.CommerceRepository;
import com.carrefourconnect.repositories.CommercantRepository;
import com.carrefourconnect.repositories.RoleRepository;
import com.carrefourconnect.repositories.UtilisateurRepository;
import com.carrefourconnect.repositories.VisiteurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

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
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UtilisateurServiceImpl service;

    private UUID id;
    private Utilisateur entity;
    private UtilisateurDTO dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        id = UUID.randomUUID();
        entity = new Visiteur();
        entity.setIduser(id);
        entity.setFavoris(new HashSet<>());
        dto = new UtilisateurDTO();
        dto.setIduser(id);
    }

    @Test
    void testFindById() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);
        
        UtilisateurDTO result = service.findById(id);
        
        assertNotNull(result);
        assertEquals(id, result.getIduser());
    }

    @Test
    void testAddFavorite() {
        Commerce commerce = new Commerce();
        UUID commerceId = UUID.randomUUID();
        commerce.setIdcommerce(commerceId);
        
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(commerceRepository.findById(commerceId)).thenReturn(Optional.of(commerce));
        
        service.addFavorite(id, commerceId);
        
        verify(repository).save(entity);
        assertTrue(entity.getFavoris().contains(commerce));
    }

    @Test
    void testRegisterVisiteur() {
        VisiteurDTO visiteurDto = new VisiteurDTO();
        visiteurDto.setPassword("rawPassword");
        
        Visiteur visiteurEntity = new Visiteur();
        
        when(visiteurMapper.toEntity(any())).thenReturn(visiteurEntity);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(visiteurRepository.save(any())).thenReturn(visiteurEntity);
        when(visiteurMapper.toDto(any())).thenReturn(new VisiteurDTO());
        
        Role role = new Role();
        role.setNom("VISITEUR");
        when(roleRepository.findByNom("VISITEUR")).thenReturn(Optional.of(role));
        
        UtilisateurDTO result = service.registerVisiteur(visiteurDto);
        
        assertNotNull(result);
        verify(passwordEncoder).encode("rawPassword");
    }

    @Test
    void testRegisterCommercant() {
        CommercantDTO commercantDto = new CommercantDTO();
        commercantDto.setPassword("p@ss123");
        
        com.carrefourconnect.entities.Commercant commercantEntity = new com.carrefourconnect.entities.Commercant();
        
        when(commercantMapper.toEntity(any())).thenReturn(commercantEntity);
        when(passwordEncoder.encode("p@ss123")).thenReturn("hashed");
        when(commercantRepository.save(any())).thenReturn(commercantEntity);
        when(commercantMapper.toDto(any())).thenReturn(new CommercantDTO());
        
        Role role = new Role();
        role.setNom("COMMERCANT");
        when(roleRepository.findByNom("COMMERCANT")).thenReturn(Optional.of(role));
        
        UtilisateurDTO result = service.registerCommercant(commercantDto);
        
        assertNotNull(result);
    }
}
