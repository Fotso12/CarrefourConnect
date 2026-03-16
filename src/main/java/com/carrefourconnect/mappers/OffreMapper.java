package com.carrefourconnect.mappers;

import com.carrefourconnect.dtos.OffreDTO;
import com.carrefourconnect.entities.Offre;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OffreMapper {
    @Mapping(source = "commerce.idcommerce", target = "idcommerce")
    OffreDTO toDto(Offre offre);

    @Mapping(source = "idcommerce", target = "commerce.idcommerce")
    Offre toEntity(OffreDTO offreDTO);
}
