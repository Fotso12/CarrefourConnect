package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.CommercantDTO;
import com.carrefourconnect.dtos.UtilisateurDTO;
import com.carrefourconnect.dtos.VisiteurDTO;
import com.carrefourconnect.entities.Commercant;
import com.carrefourconnect.entities.Utilisateur;
import com.carrefourconnect.entities.Visiteur;
import com.carrefourconnect.mappers.CommercantMapper;
import com.carrefourconnect.mappers.UtilisateurMapper;
import com.carrefourconnect.mappers.VisiteurMapper;
import com.carrefourconnect.repositories.CommercantRepository;
import com.carrefourconnect.repositories.UtilisateurRepository;
import com.carrefourconnect.repositories.VisiteurRepository;
import com.carrefourconnect.services.interfaces.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository repository;
    private final VisiteurRepository visiteurRepository;
    private final CommercantRepository commercantRepository;
    private final UtilisateurMapper mapper;
    private final VisiteurMapper visiteurMapper;
    private final CommercantMapper commercantMapper;

    @Override
    public UtilisateurDTO findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public List<UtilisateurDTO> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UtilisateurDTO save(UtilisateurDTO dto) {
        // Utilisateur est abstrait, on ne peut pas le sauvegarder directement
        // Il faut passer par les méthodes d'inscription ou utiliser une entité concrète
        throw new UnsupportedOperationException("Utiliser registerVisiteur ou registerCommercant pour sauvegarder un utilisateur.");
    }

    @Override
    public UtilisateurDTO update(UUID id, UtilisateurDTO dto) {
        return repository.findById(id).map(existing -> {
            // Note: Update logic might be tricky for inheritance, keeping it simple
            existing.setNom(dto.getNom());
            existing.setPrenom(dto.getPrenom());
            existing.setEmail(dto.getEmail());
            existing.setTelephone(dto.getTelephone());
            return mapper.toDto(repository.save(existing));
        }).orElse(null);
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public UtilisateurDTO findByEmail(String email) {
        return repository.findByEmail(email)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public void addFavorite(UUID userId, UUID commerceId) {
        // Logique complexe de favoris à implémenter si nécessaire via repository
    }

    @Override
    public void removeFavorite(UUID userId, UUID commerceId) {
        // Logique complexe de favoris à implémenter si nécessaire via repository
    }

    @Override
    public List<UUID> getFavorites(UUID userId) {
        // Retourner la liste des favoris
        return List.of();
    }

    @Override
    public UtilisateurDTO registerVisiteur(VisiteurDTO dto) {
        Visiteur entity = visiteurMapper.toEntity(dto);
        // Ici on pourrait ajouter l'encodage du mot de passe
        return visiteurMapper.toDto(visiteurRepository.save(entity));
    }

    @Override
    public UtilisateurDTO registerCommercant(CommercantDTO dto) {
        Commercant entity = commercantMapper.toEntity(dto);
        // Ici on pourrait ajouter l'encodage du mot de passe
        return commercantMapper.toDto(commercantRepository.save(entity));
    }
}
