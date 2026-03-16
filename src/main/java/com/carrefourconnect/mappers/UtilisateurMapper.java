package com.carrefourconnect.mappers;

import com.carrefourconnect.dtos.UtilisateurDTO;
import com.carrefourconnect.entities.Utilisateur;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UtilisateurMapper {
    @Mapping(source = "role.idrole", target = "idrole")
    UtilisateurDTO toDto(Utilisateur utilisateur);

}
