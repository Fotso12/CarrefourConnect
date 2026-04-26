package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.NotificationDTO;
import com.carrefourconnect.entities.Notification;
import com.carrefourconnect.mappers.NotificationMapper;
import com.carrefourconnect.repositories.NotificationRepository;
import com.carrefourconnect.repositories.UtilisateurRepository;
import com.carrefourconnect.services.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;
    private final NotificationMapper mapper;
    private final UtilisateurRepository utilisateurRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public NotificationDTO send(NotificationDTO dto) {
        log.info("Envoi d'une notification à l'utilisateur: {}", dto.getIduser());
        Notification entity = mapper.toEntity(dto);
        
        utilisateurRepository.findById(dto.getIduser())
                .ifPresent(entity::setDestinataire);
                
        Notification saved = repository.save(entity);
        NotificationDTO savedDto = mapper.toDto(saved);
        // Push via WebSocket to the specific user topic
        try {
            if (saved.getDestinataire() != null && saved.getDestinataire().getIduser() != null) {
                messagingTemplate.convertAndSend("/topic/notifications/" + saved.getDestinataire().getIduser(), savedDto);
            }
        } catch (Exception e) {
            log.warn("Impossible d'envoyer la notification via WebSocket: {}", e.getMessage());
        }
        return savedDto;
    }

    @Override
    public void sendToAdmins(NotificationDTO dto) {
        log.info("Envoi d'une notification à l'ensemble des administrateurs: {}", dto.getTitre());
        
        List<com.carrefourconnect.entities.Utilisateur> admins = utilisateurRepository.findAll().stream()
                .filter(u -> u.getRole() != null && 
                            (u.getRole().getNom().equals("ADMIN") || u.getRole().getNom().equals("ROLE_ADMIN")))
                .collect(Collectors.toList());

        for (com.carrefourconnect.entities.Utilisateur admin : admins) {
            Notification entity = mapper.toEntity(dto);
            entity.setDestinataire(admin);
            Notification saved = repository.save(entity);
            NotificationDTO savedDto = mapper.toDto(saved);
            // Push to admin via a general admin topic and also individual topics
            try {
                messagingTemplate.convertAndSend("/topic/admin/notifications", savedDto);
                messagingTemplate.convertAndSend("/topic/notifications/" + admin.getIduser(), savedDto);
            } catch (Exception e) {
                log.warn("Impossible d'envoyer la notification admin via WebSocket: {}", e.getMessage());
            }
        }
    }

    @Override
    public List<NotificationDTO> findByUser(UUID iduser) {
        log.debug("Récupération des notifications pour l'utilisateur: {}", iduser);
        return repository.findByDestinataire_IduserOrderByDateEnvoiDesc(iduser).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(UUID idnotification) {
        log.debug("Marquage de la notification {} comme lue", idnotification);
        repository.findById(idnotification).ifPresent(n -> {
            n.setLu(true);
            repository.save(n);
        });
    }

    @Override
    public long countUnread(UUID iduser) {
        return repository.countByDestinataire_IduserAndLuFalse(iduser);
    }
}
