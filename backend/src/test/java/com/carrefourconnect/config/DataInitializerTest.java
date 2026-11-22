package com.carrefourconnect.config;

import com.carrefourconnect.repositories.AbonnementRepository;
import com.carrefourconnect.repositories.CategorieRepository;
import com.carrefourconnect.repositories.CommerceRepository;
import com.carrefourconnect.repositories.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Régression : deux pods backend démarrant en même temps ne doivent ni créer de doublons
 * (garanti par les contraintes uniques) ni planter quand l'autre pod a inséré en premier.
 */
class DataInitializerTest {

    private RoleRepository roleRepository;
    private CategorieRepository categorieRepository;
    private AbonnementRepository abonnementRepository;
    private CommerceRepository commerceRepository;
    private CommandLineRunner runner;

    @BeforeEach
    void setUp() {
        roleRepository = mock(RoleRepository.class);
        categorieRepository = mock(CategorieRepository.class);
        abonnementRepository = mock(AbonnementRepository.class);
        commerceRepository = mock(CommerceRepository.class);

        // Base vide : l'initialisation va tenter d'insérer toutes les données de référence
        when(roleRepository.findByNom(anyString())).thenReturn(Optional.empty());
        when(categorieRepository.count()).thenReturn(0L);
        when(abonnementRepository.findAll()).thenReturn(Collections.emptyList());
        when(commerceRepository.findAllWithAbonnement()).thenReturn(Collections.emptyList());

        runner = new DataInitializer().initData(roleRepository, categorieRepository,
                abonnementRepository, commerceRepository);
    }

    @Test
    void baseVideCreeLesDonneesDeReference() throws Exception {
        runner.run();

        verify(roleRepository, times(3)).save(any());          // ADMIN, COMMERCANT, VISITEUR
        verify(categorieRepository, times(1)).save(any());     // Général
        verify(abonnementRepository, times(3)).save(any());    // BASIQUE, PREMIUM, GOLD
    }

    @Test
    void uneInsertionConcurrenteParUnAutrePodNeFaitPasEchouerLeDemarrage() {
        // Simule l'autre pod qui a inséré les mêmes lignes juste avant nous :
        // la base refuse les doublons (contraintes uniques).
        DataIntegrityViolationException doublon = new DataIntegrityViolationException("unique constraint");
        when(roleRepository.save(any())).thenThrow(doublon);
        when(categorieRepository.save(any())).thenThrow(doublon);
        when(abonnementRepository.save(any())).thenThrow(doublon);

        assertDoesNotThrow(() -> runner.run());
    }
}
