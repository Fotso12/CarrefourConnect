package com.carrefourconnect.services.implementations;

import com.carrefourconnect.dtos.CommerceDTO;
import com.carrefourconnect.entities.Abonnement;
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
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import com.carrefourconnect.services.interfaces.NotificationService;
import com.carrefourconnect.dtos.NotificationDTO;
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
    private final NotificationService notificationService;
    private final EmailService emailService;

    @org.springframework.beans.factory.annotation.Value("${app.base-url}")
    private String baseUrl;

    public CommerceServiceImpl(CommerceRepository repository, 
                               CommerceMapper mapper,
                               CategorieRepository categorieRepository,
                               AbonnementRepository abonnementRepository,
                               CommercantRepository commercantRepository,
                               MediaRepository mediaRepository,
                               MediaMapper mediaMapper,
                               NotificationService notificationService,
                               EmailService emailService) {
        this.repository = repository;
        this.mapper = mapper;
        this.categorieRepository = categorieRepository;
        this.abonnementRepository = abonnementRepository;
        this.commercantRepository = commercantRepository;
        this.mediaRepository = mediaRepository;
        this.mediaMapper = mediaMapper;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    private void enrichirDto(CommerceDTO dto) {
        if (dto != null && dto.getIdcommerce() != null) {
            
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
                            if (!mediaDtos.isEmpty()) {
                                dto.setImagePrincipale(mediaDtos.get(0).getUrl());
                            }
                        }
                    );
            
            // Enrichir les localisations avec lat/lon pour le frontend
            if (dto.getLocalisations() != null) {
                dto.getLocalisations().forEach(loc -> {
                    if (loc.getGeolocalisation() != null) {
                        loc.setLat(loc.getGeolocalisation().getY());
                        loc.setLon(loc.getGeolocalisation().getX());
                    }
                });
            }
            // Recharger la catégorie si elle est manquante après le mapping auto
            if (dto.getCategorie() == null && dto.getIdcategorie() != null) {
                categorieRepository.findById(dto.getIdcategorie()).ifPresent(cat -> {
                    dto.setCategorie(com.carrefourconnect.dtos.CategorieDTO.builder()
                            .idcategorie(cat.getIdcategorie())
                            .nom(cat.getNom())
                            .description(cat.getDescription())
                            .build());
                });
            }
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
        log.debug("Récupération de tous les commerces triés par priorité");
        return repository.findByStatutOrderByPriority(StatutCommerce.VALIDE).stream()
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
            lierAbonnement(entity, dto.getIdabonnement());
        }
        
        // Abonnement obligatoire
        if (entity.getAbonnement() == null) {
            log.info("Attribution de l'abonnement par défaut.");
            abonnementRepository.findAll().stream().findFirst()
                    .ifPresent(template -> lierAbonnement(entity, template.getIdabonnement()));
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
                
                // Conversion lat/lon -> Point JTS
                if (locDto.getLat() != null && locDto.getLon() != null) {
                    GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
                    loc.setGeolocalisation(factory.createPoint(new Coordinate(locDto.getLon(), locDto.getLat())));
                } else {
                    loc.setGeolocalisation(locDto.getGeolocalisation());
                }
                
                loc.setCommerce(entity);
                // Si vous avez un mapper pour Localisation, utilisez-le ici, ou faites-le manuellement
                entity.getLocalisations().add(loc);
            });
        }

        Commerce savedEntity = repository.save(entity);

        // Envoyer une notification et email aux administrateurs
        envoyerNotificationAdministrateurs(savedEntity);

        return mapper.toDto(savedEntity);
    }

    private void envoyerNotificationAdministrateurs(Commerce commerce) {
        notificationService.sendToAdmins(NotificationDTO.builder()
                .titre("Demande d'inscription de commerce")
                .message("Une nouvelle demande d'inscription pour le commerce '" + commerce.getNom() + "' a été soumise.")
                .type("NOUVEAU_COMMERCE")
                .build());

        if (commerce.getCommercant() != null) {
            emailService.envoyerNotificationNouveauCommerce(commerce.getNom(),
                    commerce.getCommercant().getNom() + " " + commerce.getCommercant().getPrenom(),
                    commerce.getCommercant().getEmail());
        }
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
                // Créer une nouvelle instance si changement de forfait ou si l'actuel a des dates erronées (ex: 2125)
                if (existingEntity.getAbonnement() == null || 
                    !existingEntity.getAbonnement().getIdabonnement().equals(dto.getIdabonnement()) ||
                    existingEntity.getAbonnement().getDateFin().getYear() > 2030) {
                    
                    lierAbonnement(existingEntity, dto.getIdabonnement());
                }
            }
            if (dto.getIduser() != null) {
                commercantRepository.findById(dto.getIduser()).ifPresent(existingEntity::setCommercant);
            }
            
            // Mise à jour des coordonnées si présentes
            if (dto.getLocalisations() != null && !dto.getLocalisations().isEmpty() && !existingEntity.getLocalisations().isEmpty()) {
                com.carrefourconnect.dtos.LocalisationDTO locDto = dto.getLocalisations().get(0);
                com.carrefourconnect.entities.Localisation existingLoc = existingEntity.getLocalisations().get(0);
                
                existingLoc.setVille(locDto.getVille());
                existingLoc.setQuartier(locDto.getQuartier());
                existingLoc.setAdresse(locDto.getAdresse());
                
                if (locDto.getLat() != null && locDto.getLon() != null) {
                    GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
                    existingLoc.setGeolocalisation(factory.createPoint(new Coordinate(locDto.getLon(), locDto.getLat())));
                }
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
        return repository.findByNomOrderByPriority(name).stream()
                .map(mapper::toDto)
                .peek(this::enrichirDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommerceDTO> findByStatut(StatutCommerce statut) {
        log.debug("Filtrage par statut trié par priorité: {}", statut);
        return repository.findByStatutOrderByPriority(statut).stream()
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
    public List<CommerceDTO> rechercher(String nom, UUID idCategorie, UUID idAbonnement, String ville, StatutCommerce statut, Double lat, Double lon, Double rayonKm) {
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
                .filter(c -> {
                    if (nom == null || nom.trim().isEmpty()) return true;
                    String search = nom.toLowerCase();
                    boolean matchName = c.getNom() != null && c.getNom().toLowerCase().contains(search);
                    boolean matchDesc = c.getDescription() != null && c.getDescription().toLowerCase().contains(search);
                    boolean matchCat = c.getCategorie() != null && c.getCategorie().getNom().toLowerCase().contains(search);
                    return matchName || matchDesc || matchCat;
                })
                .filter(c -> {
                    // Si un statut est spécifié, on le respecte, sinon on ne montre que les VALIDE pour le public
                    if (statut != null) return c.getStatut().equals(statut);
                    return c.getStatut().equals(StatutCommerce.VALIDE);
                })
                .filter(c -> idCategorie == null || (c.getCategorie() != null && c.getCategorie().getIdcategorie().equals(idCategorie)))
                .filter(c -> idAbonnement == null || (c.getAbonnement() != null && c.getAbonnement().getIdabonnement().equals(idAbonnement)))
                // Note: La ville est dans Localisation, donc on filtre via les localisations du commerce
                // Pour simplifier ici, on accepte si au moins une localisation match
                /*.filter(c -> ville == null || repository.checkVille(c.getIdcommerce(), ville))*/
                .sorted((c1, c2) -> {
                    int p1 = c1.getAbonnement() != null ? c1.getAbonnement().getPrioriteAffichage() : 0;
                    int p2 = c2.getAbonnement() != null ? c2.getAbonnement().getPrioriteAffichage() : 0;
                    return Integer.compare(p2, p1); // Descending
                })
                .map(mapper::toDto)
                .peek(this::enrichirDto)
                .collect(Collectors.toList());
    }

    @Override
    public void suspendre(UUID id, String motif) {
        changerStatut(id, StatutCommerce.SUSPENDU, "Commerce Suspendu", 
            "Votre commerce '%s' a été suspendu pour le motif suivant : %s", motif, true);
    }

    @Override
    public void rejeter(UUID id, String motif) {
        changerStatut(id, StatutCommerce.REJETE, "Inscription de Commerce Rejetée", 
            "Votre demande d'inscription pour le commerce '%s' a été rejetée pour le motif suivant : %s", motif, true);
    }

    @Override
    public void valider(UUID id) {
        changerStatut(id, StatutCommerce.VALIDE, "Commerce Validé", 
            "Félicitations ! Votre commerce '%s' a été validé et est désormais visible sur la plateforme.", null, false);
    }

    @Override
    public void reactiver(UUID id) {
        changerStatut(id, StatutCommerce.VALIDE, "Commerce Réactivé", 
            "Votre commerce '%s' a été réactivé par l'administration et est de nouveau visible.", null, true);
    }

    private void changerStatut(UUID id, StatutCommerce nouveauStatut, String titreNotif, String templateMsg, String motif, boolean estSuspension) {
        log.info("Changement de statut pour commerce ID: {} vers {}", id, nouveauStatut);
        repository.findById(id).ifPresent(c -> {
            c.setStatut(nouveauStatut);
            if (motif != null) {
                c.setMotifSuspension(motif);
            } else {
                c.setMotifSuspension(null);
            }
            repository.save(c);

            if (c.getCommercant() != null) {
                String message = String.format(templateMsg, c.getNom(), motif);
                
                // Notification in-app
                notificationService.send(NotificationDTO.builder()
                        .iduser(c.getCommercant().getIduser())
                        .titre(titreNotif)
                        .message(message)
                        .type(nouveauStatut.name())
                        .build());
                
                // Notification Email
                String email = c.getCommercant().getEmail();
                if (email != null) {
                    switch (nouveauStatut) {
                        case SUSPENDU -> emailService.envoyerEmailSuspensionCommerce(email, c.getNom(), motif);
                        case REJETE -> emailService.envoyerEmailRejetCommerce(email, c.getNom(), motif);
                        case VALIDE -> {
                            if (estSuspension) { // Reactivation case
                                emailService.envoyerEmailReactivationCommerce(email, c.getNom());
                            } else {
                                emailService.envoyerEmailValidationCommerce(email, c.getNom());
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void incrementerViews(UUID id) {
        log.debug("Incrémentation des vues pour le commerce ID: {}", id);
        repository.findById(id).ifPresent(c -> {
            Long vues = c.getNombreVues();
            c.setNombreVues(vues != null ? vues + 1 : 1L);
            repository.save(c);
        });
    }

    /**
     * Crée ou met à jour une instance d'abonnement unique pour le commerce.
     * Cette méthode garantit que le forfait dure exactement 1 mois à partir d'aujourd'hui.
     */
    private void lierAbonnement(Commerce entity, UUID idTemplate) {
        abonnementRepository.findById(idTemplate).ifPresent(template -> {
            log.info("Gestion de l'abonnement pour {} basée sur le type {}", entity.getNom(), template.getType());
            
            // Expirer les anciens abonnements actifs pour ce commerce
            if (entity.getIdcommerce() != null) {
                List<Abonnement> anciens = abonnementRepository.findByIdCommerceOrderByDateDebutDesc(entity.getIdcommerce());
                anciens.stream()
                        .filter(a -> a.getStatut() == com.carrefourconnect.utils.enums.StatutAbonnement.ACTIF)
                        .forEach(a -> {
                            a.setStatut(com.carrefourconnect.utils.enums.StatutAbonnement.EXPIRE);
                            abonnementRepository.save(a);
                        });
            }

            log.info("Création d'une nouvelle instance d'abonnement (Historisation)");
            Abonnement instance = Abonnement.builder()
                    .idCommerce(entity.getIdcommerce())
                    .type(template.getType())
                    .montant(template.getMontant())
                    .dateDebut(java.time.LocalDateTime.now())
                    .dateFin(java.time.LocalDateTime.now().plusMonths(1))
                    .statut(com.carrefourconnect.utils.enums.StatutAbonnement.ACTIF)
                    .reference("SUB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .maxPhotos(template.getMaxPhotos())
                    .offreSpecialeAutorisee(template.isOffreSpecialeAutorisee())
                    .miseEnAvant(template.isMiseEnAvant())
                    .prioriteAffichage(template.getPrioriteAffichage())
                    .lienWhatsapp(template.isLienWhatsapp())
                    .notificationPush(template.isNotificationPush())
                    .nomAffiche(template.getNomAffiche())
                    .descriptionPlan(template.getDescriptionPlan())
                    .build();
            
            Abonnement savedAbo = abonnementRepository.save(instance);
            entity.setAbonnement(savedAbo);
        });
    }
}
