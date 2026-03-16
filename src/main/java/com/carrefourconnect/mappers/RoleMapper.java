package com.carrefourconnect.mappers;

import com.carrefourconnect.dtos.RoleDTO;
import com.carrefourconnect.entities.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleDTO toDto(Role role);
    Role toEntity(RoleDTO roleDTO);
}
