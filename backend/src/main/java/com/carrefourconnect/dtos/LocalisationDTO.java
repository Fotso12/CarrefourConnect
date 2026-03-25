package com.carrefourconnect.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocalisationDTO {
    private UUID idlocalisation;
    private UUID idcommerce;
    private String ville;
    private String quartier;
    private LocalDateTime dateMiseAJour;
    private Double lat;
    private Double lon;
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Point geolocalisation;
    private String adresse;
}
