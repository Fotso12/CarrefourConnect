package com.carrefourconnect.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Utilitaire pour la gestion des JWT (génération, validation, extraction).
 */
@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${carrefourconnect.app.jwtSecret:carrefourConnectSecretKeyMustBeVeryLongAndSecureForHS512Algorithm}")
    private String jwtSecret;

    @Value("${carrefourconnect.app.jwtExpirationMs:86400000}")
    private int jwtExpirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Génère un token JWT à partir des informations d'authentification.
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Extrait le nom d'utilisateur (email) du token JWT.
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Valide le format et la signature du token JWT.
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (SecurityException e) {
            logger.error("Signature JWT invalide : {}", e.getMessage());
            logger.debug("Token reçu: {}", authToken);
        } catch (MalformedJwtException e) {
            logger.error("Token JWT invalide : {}", e.getMessage());
            logger.debug("Token reçu: {}", authToken);
        } catch (ExpiredJwtException e) {
            logger.error("Token JWT expiré : {}", e.getMessage());
            logger.debug("Token reçu: {}", authToken);
        } catch (UnsupportedJwtException e) {
            logger.error("Token JWT non supporté : {}", e.getMessage());
            logger.debug("Token reçu: {}", authToken);
        } catch (IllegalArgumentException e) {
            logger.error("Claims JWT vides : {}", e.getMessage());
            logger.debug("Token reçu: {}", authToken);
        }

        return false;
    }
}
