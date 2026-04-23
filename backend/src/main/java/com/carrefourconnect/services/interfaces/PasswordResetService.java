package com.carrefourconnect.services.interfaces;

public interface PasswordResetService {
    void createCodeForEmail(String email, String code, long codeTtlSeconds);
    String verifyCodeAndCreateToken(String email, String code, long tokenTtlSeconds);
    boolean verifyAndConsumeToken(String email, String token);
    void removeByEmail(String email);
}
