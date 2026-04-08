package com.carrefourconnect.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idnotification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iduser", nullable = false)
    private Utilisateur destinataire;

    @Column(length = 256, nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @CreationTimestamp
    @Column(name = "dateenvoi", nullable = false, updatable = false)
    private LocalDateTime dateEnvoi;

    @Builder.Default
    @Column(nullable = false)
    private boolean lu = false;

    @Column(length = 32)
    private String type; // INFO, ALERTE, VALIDATION, SUSPENSION
}
