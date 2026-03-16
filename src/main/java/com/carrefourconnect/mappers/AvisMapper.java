package com.carrefourconnect.mappers;

import com.carrefourconnect.dtos.AvisDTO;
import com.carrefourconnect.entities.Avis;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AvisMapper {
    @Mapping(source = "commerce.idcommerce", target = "idcommerce")
    @Mapping(source = "visiteur.iduser", target = "iduser")
    AvisDTO toDto(Avis avis);

    @Mapping(source = "idcommerce", target = "commerce.idcommerce")
    @Mapping(source = "iduser", target = "visiteur.iduser")
    Avis toEntity(AvisDTO avisDTO);
}
