package com.carrefourconnect.utils;

import com.carrefourconnect.repositories.AbonnementRepository;
import com.carrefourconnect.repositories.CommerceRepository;
import com.carrefourconnect.repositories.OffreRepository;
import com.carrefourconnect.repositories.PaiementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CodeGeneratorTest {

    @Mock
    private AbonnementRepository abonnementRepository;
    @Mock
    private CommerceRepository commerceRepository;
    @Mock
    private OffreRepository offreRepository;
    @Mock
    private PaiementRepository paiementRepository;

    @InjectMocks
    private CodeGenerator codeGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateAbonnementCode() {
        when(abonnementRepository.existsByReference(anyString())).thenReturn(false);
        String code = codeGenerator.generate("ABONNEMENT");
        assertTrue(code.startsWith("ABO-"));
        assertTrue(code.contains("-202")); // Check if year is present
    }

    @Test
    void testGeneratePaiementCode() {
        when(paiementRepository.existsByNumeroPaiement(anyString())).thenReturn(false);
        String code = codeGenerator.generate("PAIEMENT");
        assertTrue(code.startsWith("PAY-"));
    }

    @Test
    void testGenerateCommerceCode() {
        when(commerceRepository.existsByReference(anyString())).thenReturn(false);
        String code = codeGenerator.generate("COMMERCE");
        assertTrue(code.startsWith("COM-"));
    }

    @Test
    void testGenerateOffreCode() {
        when(offreRepository.existsByReference(anyString())).thenReturn(false);
        String code = codeGenerator.generate("OFFRE");
        assertTrue(code.startsWith("OFF-"));
    }

    @Test
    void testGenerateWithRetry() {
        // First call returns true (exists), second call returns false
        when(abonnementRepository.existsByReference(anyString()))
                .thenReturn(true)
                .thenReturn(false);

        String code = codeGenerator.generate("ABONNEMENT");
        assertTrue(code.startsWith("ABO-"));
    }

    @Test
    void testGenerateInvalidType() {
        assertThrows(IllegalArgumentException.class, () -> codeGenerator.generate("INVALID"));
    }
}
