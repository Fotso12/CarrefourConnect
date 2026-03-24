package com.carrefourconnect.mappers;

import com.carrefourconnect.dtos.VisiteurDTO;
import com.carrefourconnect.entities.Visiteur;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VisiteurMapper {
    @Mapping(source = "role.idrole", target = "idrole")
    VisiteurDTO toDto(Visiteur visiteur);

    @Mapping(source = "idrole", target = "role.idrole")
    Visiteur toEntity(VisiteurDTO visiteurDTO);
}
