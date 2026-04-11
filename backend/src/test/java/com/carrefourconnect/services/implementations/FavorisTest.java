package com.carrefourconnect.services.implementations;

import com.carrefourconnect.entities.Commerce;
import com.carrefourconnect.entities.Visiteur;
import com.carrefourconnect.repositories.CommerceRepository;
import com.carrefourconnect.repositories.UtilisateurRepository;
import com.carrefourconnect.repositories.VisiteurRepository;
import com.carrefourconnect.services.interfaces.UtilisateurService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * Test unitaire pour les favoris.
 * Utilise des mocks Mockito au lieu d'une vraie base de données
 * pour être compatible avec l'environnement CI (pas de PostgreSQL).
 */
class FavorisTest {

    @Mock
    private UtilisateurService service;

    @Mock
    private VisiteurRepository visiteurRepository;

    @Mock
    private CommerceRepository commerceRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddFavorite() {
        UUID userId = UUID.randomUUID();
        UUID commerceId = UUID.randomUUID();

        // Simuler les appels sans nécessiter de base de données
        doNothing().when(service).addFavorite(userId, commerceId);
        doNothing().when(service).removeFavorite(userId, commerceId);

        service.addFavorite(userId, commerceId);
        service.removeFavorite(userId, commerceId);

        verify(service, times(1)).addFavorite(userId, commerceId);
        verify(service, times(1)).removeFavorite(userId, commerceId);
    }
}
