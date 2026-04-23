package com.carrefourconnect.services.implementations;

import com.carrefourconnect.entities.PasswordReset;
import com.carrefourconnect.repositories.PasswordResetRepository;
import com.carrefourconnect.services.interfaces.PasswordResetService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private final PasswordResetRepository repository;

    public PasswordResetServiceImpl(PasswordResetRepository repository) {
        this.repository = repository;
    }

    @Override
    public void createCodeForEmail(String email, String code, long codeTtlSeconds) {
        long expiry = Instant.now().plusSeconds(codeTtlSeconds).getEpochSecond();
        Optional<PasswordReset> opt = repository.findByEmail(email);
        PasswordReset pr = opt.orElseGet(() -> PasswordReset.builder().email(email).build());
        pr.setCode(code);
        pr.setCodeExpiryEpoch(expiry);
        pr.setUsed(false);
        pr.setToken(null);
        pr.setTokenExpiryEpoch(null);
        repository.save(pr);
    }

    @Override
    public String verifyCodeAndCreateToken(String email, String code, long tokenTtlSeconds) {
        Optional<PasswordReset> opt = repository.findByEmail(email);
        if (opt.isEmpty()) return null;
        PasswordReset pr = opt.get();
        Long expiry = pr.getCodeExpiryEpoch();
        if (pr.getCode() == null || expiry == null || Instant.now().getEpochSecond() > expiry) return null;
        if (!pr.getCode().equals(code)) return null;

        String token = UUID.randomUUID().toString();
        pr.setToken(token);
        pr.setTokenExpiryEpoch(Instant.now().plusSeconds(tokenTtlSeconds).getEpochSecond());
        pr.setCode(null);
        pr.setCodeExpiryEpoch(null);
        repository.save(pr);
        return token;
    }

    @Override
    public boolean verifyAndConsumeToken(String email, String token) {
        Optional<PasswordReset> opt = repository.findByEmail(email);
        if (opt.isEmpty()) return false;
        PasswordReset pr = opt.get();
        Long expiry = pr.getTokenExpiryEpoch();
        if (pr.getToken() == null || expiry == null || Instant.now().getEpochSecond() > expiry) return false;
        if (!pr.getToken().equals(token)) return false;
        // consume token by deleting record
        repository.delete(pr);
        return true;
    }

    @Override
    public void removeByEmail(String email) {
        repository.deleteByEmail(email);
    }
}
