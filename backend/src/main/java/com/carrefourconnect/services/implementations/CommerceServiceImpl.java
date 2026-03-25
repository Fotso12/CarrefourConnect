package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.CommerceDTO;
import com.carrefourconnect.entities.Commerce;
import com.carrefourconnect.mappers.CommerceMapper;
import com.carrefourconnect.repositories.AbonnementRepository;
import com.carrefourconnect.repositories.CategorieRepository;
import com.carrefourconnect.repositories.CommercantRepository;
import com.carrefourconnect.repositories.CommerceRepository;
import com.carrefourconnect.repositories.MediaRepository;
import com.carrefourconnect.mappers.MediaMapper;
import com.carrefourconnect.services.interfaces.CommerceService;
import com.carrefourconnect.utils.enums.StatutCommerce;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommerceServiceImpl implements CommerceService {

    private static final Logger log = LoggerFactory.getLogger(CommerceServiceImpl.class);

    private final CommerceRepository repository;
    private final CommerceMapper mapper;
    private final CategorieRepository categorieRepository;
    private final AbonnementRepository abonnementRepository;
    private final CommercantRepository commercantRepository;
    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;

    public CommerceServiceImpl(CommerceRepository repository, 
                               CommerceMapper mapper,
                               CategorieRepository categorieRepository,
                               AbonnementRepository abonnementRepository,
                               CommercantRepository commercantRepository,
                               MediaRepository mediaRepository,
                               MediaMapper mediaMapper) {
        this.repository = repository;
        this.mapper = mapper;
        this.categorieRepository = categorieRepository;
        this.abonnementRepository = abonnementRepository;
        this.commercantRepository = commercantRepository;
        this.mediaRepository = mediaRepository;
        this.mediaMapper = mediaMapper;
    }

    private void enrichirDto(CommerceDTO dto) {
        if (dto != null && dto.getIdcommerce() != null) {
            String baseUrl = "http://localhost:8084";
            
            // Récupérer tous les médias et les mapper
            List<com.carrefourconnect.entities.Media> mediaEntities = mediaRepository.findByCommerce_Idcommerce(dto.getIdcommerce());
            List<com.carrefourconnect.dtos.MediaDTO> mediaDtos = mediaEntities.stream()
                    .map(mediaMapper::toDto)
                    .peek(m -> m.setUrl(baseUrl + m.getUrl()))
                    .collect(Collectors.toList());
            
            dto.setImages(mediaDtos);

            // Chercher l'image principale pour l'aperçu
            mediaDtos.stream()
                    .filter(com.carrefourconnect.dtos.MediaDTO::isEstPrincipale)
                    .findFirst()
                    .ifPresentOrElse(
                        m -> {
                            dto.setImagePrincipale(m.getUrl());
                            log.debug("Image principale trouvée pour {}: {}", dto.getNom(), dto.getImagePrincipale());
                        },
                        () -> {
                            // Sinon prendre la première disponible
                            if (!mediaDtos.isEmpty()) {
                                dto.setImagePrincipale(mediaDtos.get(0).getUrl());
                                log.debug("Image fallback trouvée pour {}: {}", dto.getNom(), dto.getImagePrincipale());
                            }
                        }
                    );
        }
    }

    @Override
    public CommerceDTO findById(UUID id) {
        log.debug("Récupération commerce ID: {}", id);
        CommerceDTO dto = repository.findById(id).map(mapper::toDto).orElse(null);
        enrichirDto(dto);
        return dto;
    }

    @Override
    public List<CommerceDTO> findAll() {
        log.debug("Récupération de tous les commerces");
        return repository.findAll().stream()
                .map(mapper::toDto)
                .peek(this::enrichirDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommerceDTO save(CommerceDTO dto) {
        log.info("Enregistrement d'un nouveau commerce: {}", dto.getNom());
        Commerce entity = mapper.toEntity(dto);
        
        // Sécurité : Nettoyer les objets partiels créés par MapStruct pour éviter TransientPropertyValueException
        entity.setCategorie(null);
        entity.setAbonnement(null);
        entity.setCommercant(null);
        entity.setLocalisations(new java.util.ArrayList<>());

        // Recharger les entités depuis la base
        if (dto.getIdcategorie() != null) {
            categorieRepository.findById(dto.getIdcategorie()).ifPresent(entity::setCategorie);
        }
        
        // Catégorie obligatoire
        if (entity.getCategorie() == null) {
            log.warn("Aucune catégorie valide fournie pour {}, recherche par défaut...", dto.getNom());
            categorieRepository.findAll().stream().findFirst().ifPresent(entity::setCategorie);
        }

        if (dto.getIdabonnement() != null) {
            abonnementRepository.findById(dto.getIdabonnement()).ifPresent(entity::setAbonnement);
        }
        
        // Abonnement obligatoire
        if (entity.getAbonnement() == null) {
            log.info("Attribution de l'abonnement par défaut.");
            abonnementRepository.findAll().stream().findFirst().ifPresent(entity::setAbonnement);
        }

        if (dto.getIduser() != null) {
            commercantRepository.findById(dto.getIduser()).ifPresent(entity::setCommercant);
        }

        // Statut par défaut
        if (entity.getStatut() == null) {
            log.info("Assignation du statut par défaut: EN_ATTENTE_VALIDATION");
            entity.setStatut(StatutCommerce.EN_ATTENTE_VALIDATION);
        }

        // Valeurs numériques par défaut
        if (entity.getNombreVues() == null) {
            entity.setNombreVues(0L);
        }
        if (entity.getNoteGlobale() == null) {
            entity.setNoteGlobale(java.math.BigDecimal.ZERO);
        }

        // Gérer les localisations (les re-lier à l'entité commerce pour la Cascade)
        if (dto.getLocalisations() != null) {
            dto.getLocalisations().forEach(locDto -> {
                com.carrefourconnect.entities.Localisation loc = new com.carrefourconnect.entities.Localisation();
                loc.setVille(locDto.getVille());
                loc.setQuartier(locDto.getQuartier());
                loc.setAdresse(locDto.getAdresse());
                loc.setCommerce(entity);
                // Si vous avez un mapper pour Localisation, utilisez-le ici, ou faites-le manuellement
                entity.getLocalisations().add(loc);
            });
        }

        return mapper.toDto(repository.save(entity));
    }

    @Override
    public CommerceDTO update(UUID id, CommerceDTO dto) {
        log.info("Mise à jour du commerce ID: {}", id);
        return repository.findById(id).map(existingEntity -> {
            // On ne recrée pas l'entité de zéro pour préserver les champs non présents dans le DTO
            existingEntity.setNom(dto.getNom());
            existingEntity.setDescription(dto.getDescription());
            existingEntity.setTelephone1(dto.getTelephone1());
            existingEntity.setTelephone2(dto.getTelephone2());
            existingEntity.setEmail(dto.getEmail());
            existingEntity.setSiteweb(dto.getSiteweb());
            if (dto.getStatut() != null) {
                existingEntity.setStatut(dto.getStatut());
            }
            existingEntity.setHeureOuverture(dto.getHeureOuverture());
            existingEntity.setHeureFermeture(dto.getHeureFermeture());
            
            // Re-lier les entités
            if (dto.getIdcategorie() != null) {
                categorieRepository.findById(dto.getIdcategorie()).ifPresent(existingEntity::setCategorie);
            }
            if (dto.getIdabonnement() != null) {
                abonnementRepository.findById(dto.getIdabonnement()).ifPresent(existingEntity::setAbonnement);
            }
            if (dto.getIduser() != null) {
                commercantRepository.findById(dto.getIduser()).ifPresent(existingEntity::setCommercant);
            }
            
            CommerceDTO updatedDto = mapper.toDto(repository.save(existingEntity));
            enrichirDto(updatedDto);
            return updatedDto;
        }).orElseGet(() -> {
            log.error("Commerce non trouvé pour mise à jour: {}", id);
            return null;
        });
    }

    @Override
    public void delete(UUID id) {
        log.info("Suppression du commerce ID: {}", id);
        repository.deleteById(id);
    }

    @Override
    public List<CommerceDTO> findByCategorie(UUID categorieId) {
        log.debug("Recherche par catégorie ID: {}", categorieId);
        return repository.findByCategorie_Idcategorie(categorieId).stream()
                .map(mapper::toDto)
                .peek(this::enrichirDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommerceDTO> findByCommercant(UUID commercantId) {
        log.debug("Recherche par commerçant ID: {}", commercantId);
        return repository.findByCommercant_Iduser(commercantId).stream()
                .map(mapper::toDto)
                .peek(this::enrichirDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommerceDTO> searchByName(String name) {
        log.debug("Recherche par nom contenant: {}", name);
        return repository.findByNomContainingIgnoreCase(name).stream()
                .map(mapper::toDto)
                .peek(this::enrichirDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommerceDTO> findByStatut(StatutCommerce statut) {
        log.debug("Filtrage par statut: {}", statut);
        return repository.findByStatut(statut).stream()
                .map(mapper::toDto)
                .peek(this::enrichirDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommerceDTO> findNearby(double latitude, double longitude, double distanceInKm) {
        log.info("Recherche géo: lat={}, lon={}, dist={}km", latitude, longitude, distanceInKm);
        return repository.findNearby(latitude, longitude, distanceInKm * 1000).stream()
                .map(mapper::toDto)
                .peek(this::enrichirDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommerceDTO> rechercher(String nom, UUID idCategorie, String ville, StatutCommerce statut, Double lat, Double lon, Double rayonKm) {
        log.info("Recherche avancée: nom={}, ville={}, proximité={}km", nom, ville, rayonKm);
        
        List<Commerce> resultats;
        
        // Si la proximité est demandée, on commence par là (car c'est le filtre le plus restrictif)
        if (lat != null && lon != null && rayonKm != null) {
            resultats = repository.findNearby(lat, lon, rayonKm * 1000);
        } else {
            resultats = repository.findAll();
        }

        // Appliquer les autres filtres manuellement pour cette version (ou utiliser Specification plus tard)
        return resultats.stream()
                .filter(c -> nom == null || c.getNom().toLowerCase().contains(nom.toLowerCase()))
                .filter(c -> statut == null || c.getStatut().equals(statut))
                .filter(c -> idCategorie == null || (c.getCategorie() != null && c.getCategorie().getIdcategorie().equals(idCategorie)))
                // Note: La ville est dans Localisation, donc on filtre via les localisations du commerce
                // Pour simplifier ici, on accepte si au moins une localisation match
                /*.filter(c -> ville == null || repository.checkVille(c.getIdcommerce(), ville))*/
                .map(mapper::toDto)
                .peek(this::enrichirDto)
                .collect(Collectors.toList());
    }
}
