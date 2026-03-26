package com.carrefourconnect.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private UUID idnotification;
    private UUID iduser;
    private String titre;
    private String message;
    private LocalDateTime dateEnvoi;
    private boolean lu;
    private String type;
}
