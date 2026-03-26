package com.carrefourconnect.mappers;

import com.carrefourconnect.dtos.UtilisateurDTO;
import com.carrefourconnect.entities.Utilisateur;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UtilisateurMapper {
    @Mapping(source = "role.idrole", target = "idrole")
    @Mapping(source = "role.nom", target = "role")
    UtilisateurDTO toDto(Utilisateur utilisateur);

}
