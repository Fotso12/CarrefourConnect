package com.carrefourconnect.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaDTO {
    private UUID idmedia;
    private UUID idcommerce;
    private String url;
    private String nom;
    private String typeContenu;
    private Long tailleFichier;
}
