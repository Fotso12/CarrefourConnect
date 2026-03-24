package com.carrefourconnect.security;

import com.carrefourconnect.entities.Utilisateur;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

/**
 * Implémentation personnalisée de UserDetails pour Spring Security.
 */
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private String email;
    private String nom;
    private String prenom;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(UUID id, String email, String nom, String prenom, String password,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(Utilisateur user) {
        // Supposons que Role a une méthode getNom() ou similaire
        String roleName = user.getRole() != null ? user.getRole().getNom() : "ROLE_USER";
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }
        
        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(roleName)
        );

        return new UserDetailsImpl(
                user.getIduser(),
                user.getEmail(),
                user.getNom(),
                user.getPrenom(),
                user.getPassword(),
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public UUID getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}
