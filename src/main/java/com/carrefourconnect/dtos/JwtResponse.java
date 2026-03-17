package com.carrefourconnect.dtos;

import lombok.Data;
import java.util.List;
import java.util.UUID;

/**
 * DTO pour la réponse après une authentification réussie.
 */
@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private UUID id;
    private String email;
    private String nom;
    private String prenom;
    private String role;

    public JwtResponse(String accessToken, UUID id, String email, String nom, String prenom, String role) {
        this.token = accessToken;
        this.id = id;
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
    }
}
