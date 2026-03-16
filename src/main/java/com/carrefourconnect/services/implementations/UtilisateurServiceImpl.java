package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.CommercantDTO;
import com.carrefourconnect.dtos.UtilisateurDTO;
import com.carrefourconnect.dtos.VisiteurDTO;
import com.carrefourconnect.entities.Commerce;
import com.carrefourconnect.entities.Commercant;
import com.carrefourconnect.entities.Utilisateur;
import com.carrefourconnect.entities.Visiteur;
import com.carrefourconnect.mappers.CommercantMapper;
import com.carrefourconnect.mappers.UtilisateurMapper;
import com.carrefourconnect.mappers.VisiteurMapper;
import com.carrefourconnect.repositories.CommerceRepository;
import com.carrefourconnect.repositories.CommercantRepository;
import com.carrefourconnect.repositories.UtilisateurRepository;
import com.carrefourconnect.repositories.VisiteurRepository;
import com.carrefourconnect.services.interfaces.UtilisateurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository repository;
    private final VisiteurRepository visiteurRepository;
    private final CommercantRepository commercantRepository;
    private final CommerceRepository commerceRepository;
    private final UtilisateurMapper mapper;
    private final VisiteurMapper visiteurMapper;
    private final CommercantMapper commercantMapper;

    @Override
    public UtilisateurDTO findById(UUID id) {
        log.debug("Recherche de l'utilisateur par ID: {}", id);
        return repository.findById(id)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public List<UtilisateurDTO> findAll() {
        log.debug("Récupération de tous les utilisateurs");
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UtilisateurDTO save(UtilisateurDTO dto) {
        log.warn("Tentative de sauvegarde directe d'un utilisateur abstrait");
        throw new UnsupportedOperationException("Utiliser registerVisiteur ou registerCommercant.");
    }

    @Override
    public UtilisateurDTO update(UUID id, UtilisateurDTO dto) {
        log.info("Mise à jour de l'utilisateur ID: {}", id);
        return repository.findById(id).map(existing -> {
            existing.setNom(dto.getNom());
            existing.setPrenom(dto.getPrenom());
            existing.setEmail(dto.getEmail());
            existing.setTelephone(dto.getTelephone());
            existing.setStatus(dto.getStatus());
            return mapper.toDto(repository.save(existing));
        }).orElseGet(() -> {
            log.error("Utilisateur non trouvé pour mise à jour: {}", id);
            return null;
        });
    }

    @Override
    public void delete(UUID id) {
        log.info("Suppression de l'utilisateur ID: {}", id);
        repository.deleteById(id);
    }

    @Override
    public UtilisateurDTO findByEmail(String email) {
        log.debug("Recherche de l'utilisateur par email: {}", email);
        return repository.findByEmail(email)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public void addFavorite(UUID userId, UUID commerceId) {
        log.info("Ajout du favori: Utilisateur={}, Commerce={}", userId, commerceId);
        repository.findById(userId).ifPresentOrElse(user -> {
            commerceRepository.findById(commerceId).ifPresentOrElse(commerce -> {
                user.getFavoris().add(commerce);
                repository.save(user);
            }, () -> log.error("Commerce non trouvé pour favori: {}", commerceId));
        }, () -> log.error("Utilisateur non trouvé pour favori: {}", userId));
    }

    @Override
    public void removeFavorite(UUID userId, UUID commerceId) {
        log.info("Retrait du favori: Utilisateur={}, Commerce={}", userId, commerceId);
        repository.findById(userId).ifPresentOrElse(user -> {
            commerceRepository.findById(commerceId).ifPresentOrElse(commerce -> {
                user.getFavoris().remove(commerce);
                repository.save(user);
            }, () -> log.error("Commerce non trouvé pour retrait favori: {}", commerceId));
        }, () -> log.error("Utilisateur non trouvé pour retrait favori: {}", userId));
    }

    @Override
    public List<UUID> getFavorites(UUID userId) {
        log.debug("Récupération des favoris pour l'utilisateur: {}", userId);
        return repository.findById(userId)
                .map(user -> user.getFavoris().stream()
                        .map(Commerce::getIdcommerce)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    @Override
    public UtilisateurDTO registerVisiteur(VisiteurDTO dto) {
        log.info("Inscription d'un nouveau visiteur: {}", dto.getEmail());
        Visiteur entity = visiteurMapper.toEntity(dto);
        return visiteurMapper.toDto(visiteurRepository.save(entity));
    }

    @Override
    public UtilisateurDTO registerCommercant(CommercantDTO dto) {
        log.info("Inscription d'un nouveau commerçant: {}", dto.getEmail());
        Commercant entity = commercantMapper.toEntity(dto);
        return commercantMapper.toDto(commercantRepository.save(entity));
    }
}
