package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.MediaDTO;
import com.carrefourconnect.entities.Media;
import com.carrefourconnect.mappers.MediaMapper;
import com.carrefourconnect.repositories.MediaRepository;
import com.carrefourconnect.services.interfaces.MediaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MediaServiceImpl implements MediaService {

    private static final Logger log = LoggerFactory.getLogger(MediaServiceImpl.class);

    private final MediaRepository repository;
    private final com.carrefourconnect.repositories.CommerceRepository commerceRepository;
    private final MediaMapper mapper;

    public MediaServiceImpl(MediaRepository repository, 
                            com.carrefourconnect.repositories.CommerceRepository commerceRepository,
                            MediaMapper mapper) {
        this.repository = repository;
        this.commerceRepository = commerceRepository;
        this.mapper = mapper;
    }

    @Override
    public MediaDTO findById(UUID id) {
        log.debug("Récupération média ID: {}", id);
        return repository.findById(id)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public List<MediaDTO> findAll() {
        log.debug("Récupération de tous les médias");
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public MediaDTO save(MediaDTO dto) {
        log.info("Enregistrement média: {}", dto.getNom());
        Media entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public MediaDTO update(UUID id, MediaDTO dto) {
        log.info("Mise à jour média ID: {}", id);
        return repository.findById(id).map(existing -> {
            existing.setNom(dto.getNom());
            existing.setUrl(dto.getUrl());
            existing.setTypeContenu(dto.getTypeContenu());
            existing.setTailleFichier(dto.getTailleFichier());
            existing.setEstPrincipale(dto.isEstPrincipale());
            return mapper.toDto(repository.save(existing));
        }).orElseGet(() -> {
            log.error("Média non trouvé pour mise à jour: {}", id);
            return null;
        });
    }

    @Override
    public void delete(UUID id) {
        log.info("Suppression média ID: {}", id);
        repository.deleteById(id);
    }

    @Override
    public List<MediaDTO> findByCommerce(UUID commerceId) {
        log.debug("Recherche médias commerce ID: {}", commerceId);
        return repository.findByCommerce_Idcommerce(commerceId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public MediaDTO upload(org.springframework.web.multipart.MultipartFile file, UUID commerceId, boolean estPrincipale) {
        log.info("Upload de fichier pour commerce {}: {}", commerceId, file.getOriginalFilename());
        try {
            // Récupérer le commerce et son abonnement
            com.carrefourconnect.entities.Commerce commerce = commerceRepository.findById(commerceId)
                    .orElseThrow(() -> new RuntimeException("Commerce non trouvé"));
            
            // Vérification du quota de photos
            if (commerce.getAbonnement() != null) {
                int max = commerce.getAbonnement().getMaxPhotos();
                if (max != -1) {
                    long currentCount = repository.findByCommerce_Idcommerce(commerceId).size();
                    if (currentCount >= max) {
                        log.warn("Quota de photos atteint pour le commerce {}: {}/{}", commerce.getNom(), currentCount, max);
                        throw new IllegalStateException("Limite de photos atteinte pour votre plan d'abonnement (" + max + ").");
                    }
                }
            }

            // Créer le dossier uploads s'il n'existe pas
            java.nio.file.Path root = java.nio.file.Paths.get("uploads");
            if (!java.nio.file.Files.exists(root)) {
                java.nio.file.Files.createDirectories(root);
            }

            // Générer un nom unique
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            java.nio.file.Path filePath = root.resolve(filename);
            
            // Sauvegarder le fichier sur le disque
            java.nio.file.Files.copy(file.getInputStream(), filePath);

            // Créer l'entité Media
            Media media = Media.builder()
                    .nom(file.getOriginalFilename())
                    .url("/uploads/" + filename) // URL relative servie par Spring
                    .typeContenu(file.getContentType())
                    .tailleFichier(file.getSize())
                    .estPrincipale(estPrincipale)
                    .commerce(commerce)
                    .build();

            return mapper.toDto(repository.save(media));
        } catch (Exception e) {
            log.error("Erreur lors de l'upload du fichier: {}", e.getMessage());
            throw new RuntimeException("Impossible de stocker le fichier", e);
        }
    }
}
