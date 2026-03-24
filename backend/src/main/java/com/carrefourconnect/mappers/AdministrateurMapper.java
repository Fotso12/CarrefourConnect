package com.carrefourconnect.mappers;

import com.carrefourconnect.dtos.AdministrateurDTO;
import com.carrefourconnect.entities.Administrateur;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdministrateurMapper {
    @Mapping(source = "role.idrole", target = "idrole")
    AdministrateurDTO toDto(Administrateur administrateur);

    @Mapping(source = "idrole", target = "role.idrole")
    Administrateur toEntity(AdministrateurDTO administrateurDTO);
}
