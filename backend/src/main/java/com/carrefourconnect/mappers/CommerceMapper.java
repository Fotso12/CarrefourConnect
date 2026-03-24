package com.carrefourconnect.mappers;

import com.carrefourconnect.dtos.CommerceDTO;
import com.carrefourconnect.entities.Commerce;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {LocalisationMapper.class})
public interface CommerceMapper {
    @Mapping(source = "categorie.idcategorie", target = "idcategorie")
    @Mapping(source = "abonnement.idabonnement", target = "idabonnement")
    @Mapping(source = "commercant.iduser", target = "iduser")
    CommerceDTO toDto(Commerce commerce);

    @Mapping(source = "idcategorie", target = "categorie.idcategorie")
    @Mapping(source = "idabonnement", target = "abonnement.idabonnement")
    @Mapping(source = "iduser", target = "commercant.iduser")
    Commerce toEntity(CommerceDTO commerceDTO);
}
