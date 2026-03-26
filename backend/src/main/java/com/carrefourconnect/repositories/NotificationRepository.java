package com.carrefourconnect.repositories;

import com.carrefourconnect.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByDestinataire_IduserOrderByDateEnvoiDesc(UUID iduser);
    long countByDestinataire_IduserAndLuFalse(UUID iduser);
}
