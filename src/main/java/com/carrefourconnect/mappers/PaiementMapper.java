package com.carrefourconnect.mappers;

import com.carrefourconnect.dtos.PaiementDTO;
import com.carrefourconnect.entities.Paiement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaiementMapper {
    @Mapping(source = "abonnement.idabonnement", target = "idabonnement")
    PaiementDTO toDto(Paiement paiement);

    @Mapping(source = "idabonnement", target = "abonnement.idabonnement")
    Paiement toEntity(PaiementDTO paiementDTO);
}
