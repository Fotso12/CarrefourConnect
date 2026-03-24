package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.JwtResponse;
import com.carrefourconnect.dtos.LoginRequest;
import com.carrefourconnect.security.JwtUtils;
import com.carrefourconnect.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion de l'authentification.
 * Expose l'endpoint de connexion qui génère un token JWT.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    /**
     * Authentifie un utilisateur et retourne un token JWT.
     *
     * <p>Le processus est le suivant :
     * <ol>
     *   <li>Vérification des identifiants (email + mot de passe) via Spring Security.</li>
     *   <li>Stockage de l'authentification dans le contexte de sécurité.</li>
     *   <li>Génération d'un token JWT signé.</li>
     *   <li>Retour du token avec les informations de l'utilisateur connecté.</li>
     * </ol>
     *
     * @param loginRequest Le corps de la requête contenant l'email et le mot de passe.
     * @return 200 OK avec un {@link JwtResponse} contenant le token Bearer et les infos utilisateur,
     *         ou 401 Unauthorized si les identifiants sont invalides.
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        // Authentifier l'utilisateur avec ses identifiants (email + mot de passe)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        // Définir l'authentification dans le contexte de sécurité Spring
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Générer le token JWT signé avec la clé secrète
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Récupérer les détails de l'utilisateur authentifié
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Récupérer le rôle principal de l'utilisateur
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(item -> item.getAuthority())
                .orElse("ROLE_USER");

        // Retourner la réponse JSON avec le token et les infos utilisateur
        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getNom(),
                userDetails.getPrenom(),
                role));
    }
}
