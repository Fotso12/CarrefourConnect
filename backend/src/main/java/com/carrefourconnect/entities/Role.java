package com.carrefourconnect.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idrole;

    // unique : un rôle (ADMIN, COMMERCANT, VISITEUR) ne doit exister qu'une fois, même si
    // plusieurs instances du backend démarrent en même temps (voir DataInitializer).
    @Column(length = 64, nullable = false, unique = true)
    private String nom;

    @Column(length = 256)
    private String description;
}
