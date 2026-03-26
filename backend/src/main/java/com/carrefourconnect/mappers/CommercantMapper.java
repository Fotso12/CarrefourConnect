package com.carrefourconnect.mappers;

import com.carrefourconnect.dtos.CommercantDTO;
import com.carrefourconnect.entities.Commercant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommercantMapper {
    @Mapping(source = "role.idrole", target = "idrole")
    @Mapping(source = "role.nom", target = "role")
    CommercantDTO toDto(Commercant commercant);

    @Mapping(source = "idrole", target = "role.idrole")
    Commercant toEntity(CommercantDTO commercantDTO);
}
