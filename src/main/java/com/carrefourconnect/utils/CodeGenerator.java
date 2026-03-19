package com.carrefourconnect.utils;

import com.carrefourconnect.repositories.AbonnementRepository;
import com.carrefourconnect.repositories.CommerceRepository;
import com.carrefourconnect.repositories.OffreRepository;
import com.carrefourconnect.repositories.PaiementRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CodeGenerator {

    private final AbonnementRepository abonnementRepository;
    private final CommerceRepository commerceRepository;
    private final OffreRepository offreRepository;
    private final PaiementRepository paiementRepository;

    public CodeGenerator(AbonnementRepository abonnementRepository, 
                         CommerceRepository commerceRepository, 
                         OffreRepository offreRepository, 
                         PaiementRepository paiementRepository) {
        this.abonnementRepository = abonnementRepository;
        this.commerceRepository = commerceRepository;
        this.offreRepository = offreRepository;
        this.paiementRepository = paiementRepository;
    }

    public String generate(String entityType) {
        String prefix = switch (entityType.toUpperCase()) {
            case "ABONNEMENT" -> "ABO";
            case "PAIEMENT" -> "PAY";
            case "COMMERCE" -> "COM";
            case "OFFRE" -> "OFF";
            default -> throw new IllegalArgumentException("Type d'entité inconnu: " + entityType);
        };

        long randomNumber = ThreadLocalRandom.current().nextLong(100000, 999999);
        int year = LocalDate.now().getYear();
        String code = prefix + "-" + year + "-" + randomNumber;

        if (isCodeExists(entityType, code)) {
            return generate(entityType); // Régénérer si le code existe déjà
        } else {
            return code;
        }
    }

    private boolean isCodeExists(String entityType, String code) {
        return switch (entityType.toUpperCase()) {
            case "ABONNEMENT" -> abonnementRepository.existsByReference(code);
            case "PAIEMENT" -> paiementRepository.existsByNumeroPaiement(code);
            case "COMMERCE" -> commerceRepository.existsByReference(code);
            case "OFFRE" -> offreRepository.existsByReference(code);
            default -> false;
        };
    }
}
