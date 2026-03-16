package com.carrefourconnect.mappers;

import com.carrefourconnect.dtos.CategorieDTO;
import com.carrefourconnect.entities.Categorie;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategorieMapper {
    CategorieDTO toDto(Categorie categorie);
    Categorie toEntity(CategorieDTO categorieDTO);
}
