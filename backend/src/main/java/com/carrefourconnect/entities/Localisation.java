package com.carrefourconnect.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "localisation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Localisation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idlocalisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcommerce", nullable = false)
    private Commerce commerce;

    @Column(length = 128, nullable = false)
    private String ville;

    @Column(length = 128)
    private String quartier;

    @UpdateTimestamp
    @Column(name = "datemiseajour", nullable = false)
    private LocalDateTime dateMiseAJour;

    @Column(columnDefinition = "${app.spatial.column-type:geography(Point, 4326)}")
    private Point geolocalisation;

    @Column(length = 256)
    private String adresse;
}
