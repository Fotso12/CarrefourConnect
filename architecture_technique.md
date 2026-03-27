# Architecture Technique et Guide de Développement - CarrefourConnect

Ce document résume les choix technologiques, la charte graphique et l'emplacement des logiques clés implémentées dans le projet **CarrefourConnect**.

---

## 1. Charte Graphique et Identité Visuelle
L'application utilise une esthétique **moderne et premium** basée sur le design system de Carrefour, enrichie par des concepts contemporains (Glassmorphism, Micro-animations).

- **Palette de Couleurs Principales :**
  - **Bleu Carrefour Deep (#003B71) :** Utilisé pour le header, les boutons primaires et la force de la marque.
  - **Bleu LinkedIn/Technologique (#00ADEF) :** Utilisé pour l'espace Admin, les liens et les accents de confiance.
  - **Orange Énergie (#F78F1E) :** Utilisé pour les appels à l'action (CTA), les badges et les éléments interactifs.
  - **Gris Ardoise (#1E293B) :** Couleur de fond pour le sidebar admin, offrant un contraste professionnel.
- **Typographie :** Utilisation de polices sans-serif modernes (Inter/Roboto) pour une lisibilité maximale.
- **Composants UI :** Usage intensif de **Tailwind CSS** pour des layouts flexibles et responsifs.

---

## 2. Architecture Backend (Spring Boot 3.x)
Le backend suit une architecture en couches (N-Tier) standard, assurant une séparation nette entre les données et la logique métier.

### Logiques Essentielles :
- **Notifications :**
  - **Interface :** `com.carrefourconnect.services.interfaces.NotificationService`
  - **Implémentation :** `NotificationServiceImpl.java`
  - **Détail :** Gère l'envoi de messages in-app et le comptage des messages non lus.
- **Simulation de Paiement :**
  - **Service :** `PaiementServiceImpl.java`
  - **Détail :** Enregistre les transactions liées aux abonnements. La logique de simulation génère une référence unique (PAY-...) et valide le statut.
- **Validation et Suspension d'un Commerce :**
  - **Service :** `CommerceServiceImpl.java` (Méthodes `valider()` et `suspendre()`).
  - **Détail :** Change le `StatutCommerce` et déclenche une notification automatique au commerçant.
- **Suspension d'un Utilisateur :**
  - **Service :** `UtilisateurServiceImpl.java` (Méthode `suspendre()`).
  - **Détail :** Désactive le compte en changeant le booléen `actif`, empêchant toute connexion ultérieure via JWT.
- **Gestion des Catégories :** `CategorieServiceImpl.java` (CRUD complet).
- **Gestion des Abonnements :** 
  - `AbonnementServiceImpl.java` pour le catalogue.
  - `CommerceServiceImpl.java` (Méthode `lierAbonnement`) pour la logique de calcul de date d'expiration (systématiquement +30 jours).

### Géolocalisation et OpenStreetMap (Backend) :
Le backend utilise **PostGIS** (extension spatiale de PostgreSQL) et la bibliothèque **JTS (Java Topology Suite)**.
- **Entité :** `Localisation.java` avec un champ `Point` (Geometry).
- **Service :** `LocalisationServiceImpl.java` gère le stockage des coordonnées.

---

## 3. Architecture Frontend (Angular 18+)
Le frontend est conçu de manière modulaire, séparant le "Cœur" (services/DTOs) des "Fonctionnalités" (pages).

### Logiques Géographiques (OpenStreetMap) :
La cartographie est implémentée via **Leaflet**, une bibliothèque open-source performante.
- **Composant Carte :** `src/app/partages/composants/carte/carte.component.ts`
- **Source des données :** Tuiles OpenStreetMap standards (`{s}.tile.openstreetmap.org`).
- **Paiement (UI) :** Les formulaires de paiement se trouvent dans les modules d'inscription ou de profil (ex: `gestion-abonnements`).
- **Tracé de Distance & Itinéraire :**
  - Implémenté dans `carte.component.ts` via l'input `routePoints`.
  - Le tracé utilise des polylines Leaflet avec une couleur thématique orange (#F78F1E).
  - Le calcul de distance et l'estimation du temps se font via le service Leaflet Routing ou des helpers utilitaires (calcul Haversine ou API OSRM).

### Interface et Composants Partagés :
- **Modals :** Définis dans `src/app/partages/composants/modal`. Ce composant est générique et utilisé partout via une projection de contenu (`<ng-content>`).
- **About (À propos) :** Localisé dans `src/app/fonctionnalites/accueil/a-propos`.
- **Gestion des Données (Modèles) :** 
  - Actuellement, le projet utilise le **typage implicite** via le mot-clé `any` de TypeScript.
  - Cela signifie que les objets JSON reçus du backend sont utilisés directement par leurs propriétés (ex: `commerce.nom`) sans interface formelle. 
  - *Note pour l'évolution :* L'utilisation d'interfaces TypeScript explicites (ex: `interface Commerce { ... }`) est la pratique recommandée pour une meilleure robustesse et l'autocomplétion.
- **Mapping DTO/Entités :** Les échanges se font via JSON natif. Le mapping est automatique lors des appels `http.get<any>()`.

---

## 4. Sécurité
- **JWT (JSON Web Token) :** La sécurité est centralisée dans `SecurityConfig.java` (Backend) et `auth.service.ts` (Frontend).
- **Filtre :** `JwtAuthenticationFilter.java` intercepte chaque requête pour valider l'identité.

---

## 5. Résumé des Emplacements Clés (Pour le Jury)
| Fonctionnalité | Backend (Service Impl) | Frontend (Composant/Service) |
| :--- | :--- | :--- |
| **Notifications** | `NotificationServiceImpl.java` | `notification.service.ts` |
| **Paiement** | `PaiementServiceImpl.java` | `abonnement.service.ts` |
| **Carte / OSM** | N/A (Data only) | `carte.component.ts` |
| **Itinéraires** | N/A | `carte.component.ts` |
| **Validation Commerce** | `CommerceServiceImpl.java` | `validation-commerces.component.ts` |
| **Gestion Abonnements** | `AbonnementServiceImpl.java` | `gestion-abonnements.ts` |
| **Modals** | N/A | `modal.component.ts` |
