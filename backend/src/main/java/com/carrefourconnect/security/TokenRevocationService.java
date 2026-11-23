package com.carrefourconnect.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

/**
 * Liste noire des jetons JWT révoqués (déconnexion).
 *
 * Un JWT est "sans état" : le serveur ne garde aucune session, donc un jeton reste valable
 * jusqu'à son expiration, même après la déconnexion. Pour une vraie déconnexion, on mémorise
 * le jeton révoqué jusqu'à son expiration naturelle, puis on l'oublie.
 * On ne stocke jamais le jeton lui-même, seulement son empreinte SHA-256.
 */
public interface TokenRevocationService {

    /** Révoque le jeton jusqu'à sa date d'expiration. */
    void revoke(String token, Instant expiresAt);

    /** Vrai si le jeton a été révoqué (déconnexion). */
    boolean isRevoked(String token);

    /** Empreinte SHA-256 (hexadécimal) du jeton : c'est elle qui sert de clé. */
    static String fingerprint(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}
