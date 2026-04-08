package com.carrefourconnect.services.implementations;

import com.carrefourconnect.entities.Commerce;
import com.carrefourconnect.entities.Visiteur;
import com.carrefourconnect.repositories.CommerceRepository;
import com.carrefourconnect.repositories.UtilisateurRepository;
import com.carrefourconnect.repositories.VisiteurRepository;
import com.carrefourconnect.services.interfaces.UtilisateurService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@SpringBootTest
public class FavorisTest {

    @Autowired
    private UtilisateurService service;

    @Autowired
    private VisiteurRepository visiteurRepository;

    @Autowired
    private CommerceRepository commerceRepository;

    @Test
    public void testAddFavorite() {
        try {
            Visiteur v = visiteurRepository.findAll().get(0);
            Commerce c = commerceRepository.findAll().get(0);
            System.out.println("TESTING FAVORIS FOR: User=" + v.getIduser() + " Commerce=" + c.getIdcommerce());
            service.addFavorite(v.getIduser(), c.getIdcommerce());
            System.out.println("SUCCESSFULLY ADDED FAVORITE");
            
            service.removeFavorite(v.getIduser(), c.getIdcommerce());
            System.out.println("SUCCESSFULLY REMOVED FAVORITE");
        } catch (Exception e) {
            System.err.println("EXCEPTION THROWN CAUGHT IN TEST:");
            e.printStackTrace();
        }
    }
}
