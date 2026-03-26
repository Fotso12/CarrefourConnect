package com.carrefourconnect.mappers;

import com.carrefourconnect.dtos.NotificationDTO;
import com.carrefourconnect.entities.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    @Mapping(source = "destinataire.iduser", target = "iduser")
    NotificationDTO toDto(Notification notification);

    @Mapping(target = "destinataire", ignore = true)
    @Mapping(target = "dateEnvoi", ignore = true)
    Notification toEntity(NotificationDTO dto);
}
