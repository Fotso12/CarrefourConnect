package com.carrefourconnect.mappers;

import com.carrefourconnect.dtos.LocalisationDTO;
import com.carrefourconnect.entities.Localisation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocalisationMapper {
    @Mapping(source = "commerce.idcommerce", target = "idcommerce")
    LocalisationDTO toDto(Localisation localisation);

    @Mapping(source = "idcommerce", target = "commerce.idcommerce")
    Localisation toEntity(LocalisationDTO localisationDTO);
}
