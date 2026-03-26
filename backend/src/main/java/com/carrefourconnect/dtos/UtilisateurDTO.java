package com.carrefourconnect.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurDTO {
    private java.util.UUID iduser;
    private java.util.UUID idrole;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String password;
    private java.time.LocalDateTime dateCreation;
    private String status;
    private String role;
}
