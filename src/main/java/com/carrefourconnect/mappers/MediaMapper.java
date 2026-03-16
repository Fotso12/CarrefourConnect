package com.carrefourconnect.mappers;

import com.carrefourconnect.dtos.MediaDTO;
import com.carrefourconnect.entities.Media;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MediaMapper {
    @Mapping(source = "commerce.idcommerce", target = "idcommerce")
    MediaDTO toDto(Media media);

    @Mapping(source = "idcommerce", target = "commerce.idcommerce")
    Media toEntity(MediaDTO mediaDTO);
}
