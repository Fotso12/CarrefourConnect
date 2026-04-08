package com.carrefourconnect.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "utilisateur")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID iduser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idrole", nullable = false)
    private Role role;

    @Column(length = 128, nullable = false)
    private String nom;

    @Column(length = 128, nullable = false)
    private String prenom;

    @Column(length = 256, nullable = false, unique = true)
    private String email;

    @Column(length = 20, nullable = false)
    private String telephone;

    @Column(length = 256, nullable = false)
    private String password;

    @CreationTimestamp
    @Column(name = "datecreation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(length = 32, nullable = false)
    private String status;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "favori",
        joinColumns = @JoinColumn(name = "iduser"),
        inverseJoinColumns = @JoinColumn(name = "idcommerce")
    )
    private Set<Commerce> favoris = new HashSet<>();

    @Column(name = "motifsuspension", columnDefinition = "TEXT")
    private String motifSuspension;
}
