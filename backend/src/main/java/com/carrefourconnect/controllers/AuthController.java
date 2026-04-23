package com.carrefourconnect.controllers;

import com.carrefourconnect.dtos.JwtResponse;
import com.carrefourconnect.dtos.LoginRequest;
import com.carrefourconnect.services.interfaces.UtilisateurService;
import com.carrefourconnect.services.implementations.EmailService;
import com.carrefourconnect.security.JwtUtils;
import com.carrefourconnect.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.carrefourconnect.services.interfaces.PasswordResetService;

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

        @Autowired
        UtilisateurService utilisateurService;

        @Autowired
        EmailService emailService;

        @Autowired
        PasswordResetService passwordResetService;

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
        @PostMapping(value = {"/login", "/signin"})
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

        // ---------- Mot de passe oublié (FR endpoints) ----------

        static class MotDePasseOublieRequest {
                public String email;
        }

        static class VerifierCodeRequest {
                public String email;
                public String code;
        }

        static class ReinitialiserMotDePasseRequest {
                public String email;
                public String token;
                public String nouveauMotDePasse;
        }

        @PostMapping(value = "/mot-de-passe-oublie", consumes = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<?> motDePasseOublie(@RequestBody MotDePasseOublieRequest req) {
                final String email = req.email == null ? "" : req.email.trim().toLowerCase();

                // Generate 5-digit code
                int codeInt = 10000 + (int)(Math.random() * 90000);
                final String code = Integer.toString(codeInt);

                // persist code with TTL (15 minutes)
                passwordResetService.createCodeForEmail(email, code, 15 * 60);

                // send email (async). If sending fails, we still return 200 to avoid information leakage
                emailService.envoyerCodeReinitialisation(email, code);

                return ResponseEntity.ok(Map.of("message", "Si l'email existe, un code de vérification a été envoyé."));
        }

        @PostMapping(value = "/verifier-code", consumes = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<?> verifierCode(@RequestBody VerifierCodeRequest req) {
                final String email = req.email == null ? "" : req.email.trim().toLowerCase();
                final String code = req.code == null ? "" : req.code.trim();

                final String token = passwordResetService.verifyCodeAndCreateToken(email, code, 15 * 60);
                if (token == null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Code invalide ou expiré."));
                }
                return ResponseEntity.ok(Map.of("token", token));
        }

        @PostMapping(value = "/reinitialiser-mot-de-passe", consumes = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<?> reinitialiserMotDePasse(@RequestBody ReinitialiserMotDePasseRequest req) {
                final String email = req.email == null ? "" : req.email.trim().toLowerCase();
                final String token = req.token == null ? "" : req.token.trim();
                final String nouveau = req.nouveauMotDePasse == null ? "" : req.nouveauMotDePasse;

                boolean tokenOk = passwordResetService.verifyAndConsumeToken(email, token);
                if (!tokenOk) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Token invalide ou expiré."));
                }

                boolean ok = utilisateurService.reinitialiserMotDePasseParEmail(email, nouveau);

                if (!ok) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Impossible de réinitialiser le mot de passe pour cet email."));
                }

                return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès."));
        }
}
