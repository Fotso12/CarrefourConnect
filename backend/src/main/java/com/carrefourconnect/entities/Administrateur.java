package com.carrefourconnect.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "administrateur")
@Data
@EqualsAndHashCode(callSuper = true)
public class Administrateur extends Utilisateur {

    // L'entité n'a pas de champs spécifiques, elle hérite tout de Utilisateur.
}
