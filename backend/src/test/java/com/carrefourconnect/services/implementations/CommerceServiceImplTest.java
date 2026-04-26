package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.CommerceDTO;
import com.carrefourconnect.dtos.NotificationDTO;
import com.carrefourconnect.entities.Categorie;
import com.carrefourconnect.entities.Commercant;
import com.carrefourconnect.entities.Commerce;
import com.carrefourconnect.mappers.CommerceMapper;
import com.carrefourconnect.mappers.MediaMapper;
import com.carrefourconnect.repositories.AbonnementRepository;
import com.carrefourconnect.repositories.CategorieRepository;
import com.carrefourconnect.repositories.CommercantRepository;
import com.carrefourconnect.repositories.CommerceRepository;
import com.carrefourconnect.repositories.MediaRepository;
import com.carrefourconnect.services.interfaces.NotificationService;
import com.carrefourconnect.utils.enums.StatutCommerce;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

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
    @Mock
    private CategorieRepository categorieRepository;
    @Mock
    private AbonnementRepository abonnementRepository;
    @Mock
    private CommercantRepository commercantRepository;
    @Mock
    private MediaRepository mediaRepository;
    @Mock
    private MediaMapper mediaMapper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private EmailService emailService;

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
        entity.setNom("Test Commerce");
        entity.setStatut(StatutCommerce.EN_ATTENTE_VALIDATION);
        
        Commercant commercant = new Commercant();
        commercant.setIduser(UUID.randomUUID());
        commercant.setEmail("test@owner.com");
        entity.setCommercant(commercant);

        dto = new CommerceDTO();
        dto.setIdcommerce(id);
        dto.setNom("Test Commerce");
        
        // Setup base URL for enrichment
        ReflectionTestUtils.setField(service, "baseUrl", "http://localhost:8084");
    }

    @Test
    void testFindById() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);
        
        CommerceDTO result = service.findById(id);
        
        assertNotNull(result);
        assertEquals(id, result.getIdcommerce());
        verify(repository).findById(id);
    }

    @Test
    void testSave() {
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(any(Commerce.class))).thenReturn(entity);
        when(mapper.toDto(any(Commerce.class))).thenReturn(dto);
        when(categorieRepository.findAll()).thenReturn(Collections.singletonList(new Categorie()));
        when(abonnementRepository.findAll()).thenReturn(Collections.emptyList());

        CommerceDTO result = service.save(dto);

        assertNotNull(result);
        verify(notificationService).sendToAdmins(any(NotificationDTO.class));
        verify(repository).save(any(Commerce.class));
    }

    @Test
    void testValider() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(any(Commerce.class))).thenReturn(entity);

        service.valider(id);

        assertEquals(StatutCommerce.VALIDE, entity.getStatut());
        verify(notificationService).send(any(NotificationDTO.class));
        verify(emailService).envoyerEmailValidationCommerce(anyString(), anyString());
        verify(repository).save(entity);
    }

    @Test
    void testSuspendre() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(any(Commerce.class))).thenReturn(entity);

        String motif = "Non respect des règles";
        service.suspendre(id, motif);

        assertEquals(StatutCommerce.SUSPENDU, entity.getStatut());
        assertEquals(motif, entity.getMotifSuspension());
        verify(emailService).envoyerEmailSuspensionCommerce(eq("test@owner.com"), anyString(), eq(motif));
    }

    @Test
    void testRejeter() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(any(Commerce.class))).thenReturn(entity);

        String motif = "Documents invalides";
        service.rejeter(id, motif);

        assertEquals(StatutCommerce.REJETE, entity.getStatut());
        verify(emailService).envoyerEmailRejetCommerce(anyString(), anyString(), eq(motif));
    }

    @Test
    void testReactiver() {
        entity.setStatut(StatutCommerce.SUSPENDU);
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(any(Commerce.class))).thenReturn(entity);

        service.reactiver(id);

        assertEquals(StatutCommerce.VALIDE, entity.getStatut());
        verify(emailService).envoyerEmailReactivationCommerce(anyString(), anyString());
    }

    @Test
    void testIncrementerViews() {
        entity.setNombreVues(10L);
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        service.incrementerViews(id);

        assertEquals(11L, entity.getNombreVues());
        verify(repository).save(entity);
    }
}
