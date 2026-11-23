package com.carrefourconnect.security;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Liste noire des jetons : versions en mémoire et Redis (Redis simulé ici ;
 * voir RedisIntegrationTest pour un vrai Redis).
 */
class TokenRevocationServiceTest {

    @Test
    void empreinteStableEtSansLeJetonEnClair() {
        String empreinte = TokenRevocationService.fingerprint("abc.def.ghi");
        assertThat(empreinte).hasSize(64).isEqualTo(TokenRevocationService.fingerprint("abc.def.ghi"));
        assertThat(empreinte).doesNotContain("abc");
    }

    @Test
    void enMemoireUnJetonRevoqueEstRefuseLesAutresNon() {
        InMemoryTokenRevocationService service = new InMemoryTokenRevocationService();
        service.revoke("jeton-1", Instant.now().plusSeconds(60));

        assertThat(service.isRevoked("jeton-1")).isTrue();
        assertThat(service.isRevoked("jeton-2")).isFalse();
    }

    @Test
    void enMemoireUnJetonDejaExpireNestPasMemorise() {
        InMemoryTokenRevocationService service = new InMemoryTokenRevocationService();
        service.revoke("jeton-expire", Instant.now().minusSeconds(1));

        assertThat(service.isRevoked("jeton-expire")).isFalse();
    }

    @Test
    @SuppressWarnings("unchecked")
    void redisStockeLEmpreinteAvecUneDureeDeVieEgaleAuTempsRestant() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valeurs = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(valeurs);
        RedisTokenRevocationService service = new RedisTokenRevocationService(redis);

        service.revoke("jeton", Instant.now().plus(Duration.ofMinutes(10)));

        String cle = RedisTokenRevocationService.PREFIX + TokenRevocationService.fingerprint("jeton");
        verify(valeurs).set(eq(cle), eq("1"), any(Duration.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void redisNeStockePasUnJetonDejaExpire() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valeurs = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(valeurs);

        new RedisTokenRevocationService(redis).revoke("jeton", Instant.now().minusSeconds(5));

        verify(valeurs, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void redisInjoignableLaisseLeJetonPasser() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        when(redis.hasKey(anyString())).thenThrow(new IllegalStateException("connexion refusée"));

        assertThat(new RedisTokenRevocationService(redis).isRevoked("jeton")).isFalse();
    }
}
