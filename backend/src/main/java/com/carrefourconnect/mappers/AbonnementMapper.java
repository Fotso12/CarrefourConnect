package com.carrefourconnect.mappers;

import com.carrefourconnect.dtos.AbonnementDTO;
import com.carrefourconnect.entities.Abonnement;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AbonnementMapper {
    AbonnementDTO toDto(Abonnement abonnement);
    Abonnement toEntity(AbonnementDTO abonnementDTO);
}
