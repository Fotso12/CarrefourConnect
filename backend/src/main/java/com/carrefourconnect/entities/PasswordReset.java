package com.carrefourconnect.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "password_reset")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordReset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email", length = 256, nullable = false)
    private String email;

    @Column(name = "code", length = 16)
    private String code;

    @Column(name = "code_expiry_epoch")
    private Long codeExpiryEpoch;

    @Column(name = "token", length = 128, unique = true)
    private String token;

    @Column(name = "token_expiry_epoch")
    private Long tokenExpiryEpoch;

    @Column(name = "used")
    private Boolean used = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
