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
import com.carrefourconnect.repositories.*;
import com.carrefourconnect.services.interfaces.UtilisateurService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UtilisateurServiceImpl implements UtilisateurService {

    private static final Logger log = LoggerFactory.getLogger(UtilisateurServiceImpl.class);

    private final UtilisateurRepository repository;
    private final VisiteurRepository visiteurRepository;
    private final CommercantRepository commercantRepository;
    private final CommerceRepository commerceRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UtilisateurMapper mapper;
    private final VisiteurMapper visiteurMapper;
    private final CommercantMapper commercantMapper;

    public UtilisateurServiceImpl(UtilisateurRepository repository,
                                  VisiteurRepository visiteurRepository,
                                  CommercantRepository commercantRepository,
                                  CommerceRepository commerceRepository,
                                  RoleRepository roleRepository,
                                  PasswordEncoder passwordEncoder,
                                  UtilisateurMapper mapper,
                                  VisiteurMapper visiteurMapper,
                                  CommercantMapper commercantMapper) {
        this.repository = repository;
        this.visiteurRepository = visiteurRepository;
        this.commercantRepository = commercantRepository;
        this.commerceRepository = commerceRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
        this.visiteurMapper = visiteurMapper;
        this.commercantMapper = commercantMapper;
    }

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
    public List<UtilisateurDTO> findAllNonAdmins() {
        log.debug("Récupération des utilisateurs non-administrateurs");
        return repository.findAll().stream()
                .filter(u -> u.getRole() != null && !u.getRole().getNom().equals("ADMIN") && !u.getRole().getNom().equals("ROLE_ADMIN"))
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
            existing.setMotifSuspension(dto.getMotifSuspension());
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
    public void suspendre(UUID id, String motif) {
        log.info("Suspension de l'utilisateur ID: {} pour motif: {}", id, motif);
        repository.findById(id).ifPresent(u -> {
            u.setStatus("SUSPENDU");
            u.setMotifSuspension(motif);
            repository.save(u);
        });
    }

    @Override
    public void activer(UUID id) {
        log.info("Activation de l'utilisateur ID: {}", id);
        repository.findById(id).ifPresent(u -> {
            u.setStatus("ACTIF");
            u.setMotifSuspension(null);
            repository.save(u);
        });
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
        
        // Hashage du mot de passe avant sauvegarde
        if (dto.getPassword() != null) {
            entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        
        entity.setStatus("ACTIF");
        
        // Attribution du rôle VISITEUR par défaut
        roleRepository.findByNom("VISITEUR")
                .ifPresent(entity::setRole);
                
        return visiteurMapper.toDto(visiteurRepository.save(entity));
    }

    @Override
    public UtilisateurDTO registerCommercant(CommercantDTO dto) {
        log.info("Inscription d'un nouveau commerçant: {}", dto.getEmail());
        Commercant entity = commercantMapper.toEntity(dto);
        
        // Hashage du mot de passe avant sauvegarde
        if (dto.getPassword() != null) {
            entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        
        entity.setStatus("ACTIF");
        
        // Attribution du rôle COMMERCANT par défaut
        roleRepository.findByNom("COMMERCANT")
                .ifPresent(entity::setRole);
                
        return commercantMapper.toDto(commercantRepository.save(entity));
    }
}
