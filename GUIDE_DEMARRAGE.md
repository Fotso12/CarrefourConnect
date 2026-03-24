# 📚 Guide de démarrage — CarrefourConnect

Ce document explique la structure du projet, l'architecture du backend, et comment démarrer les différents modules.

---

## 0. Structure du projet (Monorepo)

Le projet est maintenant organisé en "Monorepo" pour regrouper le backend et les frontends :

- `backend/` : API Spring Boot (Java).
- `frontend-web/` : Application Web.
- `frontend-mobile/` : Application Mobile.

> [!IMPORTANT]
> Pour toute commande Maven (ex: `./mvnw`), vous devez d'abord vous déplacer dans le dossier backend : `cd backend`.

---

## 1. Ordre de création des tables (contraintes de clés étrangères)

Les entités ont des dépendances entre elles. Il faut respecter l'ordre suivant pour éviter les erreurs de clés étrangères.

> [!IMPORTANT]
> Si tu utilises `spring.jpa.hibernate.ddl-auto=create` ou `create-drop`, Hibernate gère l'ordre automatiquement. Mais si tu crées les tables manuellement en SQL, **respecte impérativement cet ordre**.

```mermaid
graph TD
    Role --> Utilisateur
    Utilisateur --> Visiteur
    Utilisateur --> Commercant
    Utilisateur --> Administrateur
    Abonnement --> Commerce
    Categorie --> Commerce
    Commercant --> Commerce
    Commerce --> Avis
    Visiteur --> Avis
    Commerce --> Media
    Commerce --> Localisation
    Commerce --> Offre
    Abonnement --> Paiement
    Utilisateur --> Favori["favori (table de jointure)"]
    Commerce --> Favori
```

### Ordre de création recommandé

| Étape | Table | Dépend de |
|-------|-------|-----------|
| 1 | `role` | — |
| 2 | `utilisateur` | `role` |
| 3 | `visiteur` | `utilisateur` |
| 4 | `commercant` | `utilisateur` |
| 5 | `administrateur` | `utilisateur` |
| 6 | `categorie` | — |
| 7 | `abonnement` | — |
| 8 | `commerce` | `categorie`, `abonnement`, `commercant` |
| 9 | `favori` *(table de jointure)* | `utilisateur`, `commerce` |
| 10 | `localisation` | `commerce` |
| 11 | `media` | `commerce` |
| 12 | `offre` | `commerce` |
| 13 | `avis` | `commerce`, `visiteur` |
| 14 | `paiement` | `abonnement` |

---

## 2. Architecture de la sécurité (JWT)

### Flux d'authentification

```
Client                    Serveur
  |                          |
  |  POST /api/auth/login    |
  |  { email, password }     |
  |------------------------->|
  |                          |-- AuthController
  |                          |-- AuthenticationManager (vérifie les identifiants)
  |                          |-- UserDetailsServiceImpl (charge l'utilisateur depuis la BDD)
  |                          |-- JwtUtils (génère le token)
  |  200 OK                  |
  |  { token, type, ... }    |
  |<-------------------------|
  |                          |
  |  GET /api/commerces      |
  |  Authorization: Bearer <token>
  |------------------------->|
  |                          |-- JwtAuthenticationFilter (extrait et valide le token)
  |                          |-- UserDetailsServiceImpl (charge l'utilisateur)
  |                          |-- SecurityContextHolder (authentification OK)
  |  200 OK avec données     |
  |<-------------------------|
```

### Rôles des fichiers créés

| Fichier | Package | Rôle |
|---------|---------|------|
| `SecurityConfig.java` | `config` | Configure les règles de sécurité, les routes autorisées et le filtre JWT |
| `UserDetailsImpl.java` | `security` | Représente l'utilisateur connecté pour Spring Security |
| `UserDetailsServiceImpl.java` | `security` | Charge l'utilisateur depuis la BDD via son email |
| `JwtUtils.java` | `security` | Génère, valide et décode les tokens JWT |
| `AuthEntryPointJwt.java` | `security` | Renvoie un 401 si l'accès est refusé |
| `JwtAuthenticationFilter.java` | `security` | Intercepte chaque requête HTTP pour vérifier le token |
| `AuthController.java` | `controllers` | Expose `POST /api/auth/login` |
| `LoginRequest.java` | `dtos` | Corps de la requête de connexion |
| `JwtResponse.java` | `dtos` | Corps de la réponse avec le token JWT |

### Routes accessibles sans authentification

```
POST /api/auth/login        → Connexion
GET  /swagger-ui/**         → Documentation Swagger
GET  /v3/api-docs/**        → API Docs JSON
```

Toutes les autres routes nécessitent un token `Authorization: Bearer <token>`.

---

## 3. Inscription d'un nouvel utilisateur

L'inscription se fait via `UtilisateurController`. Le mot de passe est **automatiquement haché** en BCrypt et le rôle est attribué par défaut.

| Route | Rôle attribué |
|-------|---------------|
| `POST /api/utilisateurs/inscription/visiteur` | `VISITEUR` |
| `POST /api/utilisateurs/inscription/commercant` | `COMMERCANT` |

**Exemple de requête (Visiteur) :**
```json
{
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "jean.dupont@email.com",
  "telephone": "0601020304",
  "password": "MonMotDePasseSecurise",
  "preference": "Gastronomie, Sport"
}
```

---

## 4. Configuration JWT (`application.properties`)

Tu peux personnaliser ces propriétés dans ton `application.properties` :

```properties
# Clé secrète pour signer les tokens JWT (minimum 64 caractères)
carrefourconnect.app.jwtSecret=carrefourConnectSecretKeyMustBeVeryLongAndSecureForHS512Algorithm

# Durée de validité du token en millisecondes (86400000 = 24 heures)
carrefourconnect.app.jwtExpirationMs=86400000
```

---

## 4. Comment créer le premier administrateur

### Étape 1 — Insérer le rôle ADMIN

```sql
INSERT INTO role (idrole, nom, description)
VALUES (gen_random_uuid(), 'ADMIN', 'Administrateur de la plateforme')
ON CONFLICT DO NOTHING;
```

### Étape 1 — Insérer le rôle ADMIN

```sql
INSERT INTO role (idrole, nom, description)
VALUES (gen_random_uuid(), 'ADMIN', 'Administrateur de la plateforme');
```

### Étape 2 — Générer un hash BCrypt

Tu **dois** envoyer un mot de passe hashé avec BCrypt, pas en clair.  
Utilise un outil en ligne : [bcrypt-generator.com](https://bcrypt-generator.com) avec ton mot de passe (ex. `Admin@1234`).

Le hash ressemble à : `$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`

### Étape 3 — Insérer l'utilisateur administrateur

Remplace `$2a$10$REMPLACE_PAR_TON_HASH_BCRYPT` par le hash généré à l'étape 2 :

```sql
INSERT INTO utilisateur (iduser, idrole, nom, prenom, email, telephone, password, datecreation, status)
SELECT
  gen_random_uuid(),
  r.idrole,
  'Admin',
  'Super',
  'admin@carrefourconnect.com',
  '0600000000',
  '$2a$10$REMPLACE_PAR_TON_HASH_BCRYPT',
  NOW(),
  'ACTIF'
FROM role r
WHERE r.nom = 'ADMIN'
LIMIT 1;
```

### Étape 4 — Lier à la table administrateur (héritage JOINED)

```sql
INSERT INTO administrateur (iduser)
SELECT iduser FROM utilisateur WHERE email = 'admin@carrefourconnect.com';
```

### Étape 4 — Se connecter via l'API

Requête HTTP avec Postman ou curl :

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@carrefourconnect.com",
  "password": "Admin@1234"
}
```

Réponse attendue :

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "email": "admin@carrefourconnect.com",
  "nom": "Admin",
  "prenom": "Super",
  "role": "ROLE_ADMIN"
}
```

### Étape 5 — Utiliser le token pour les routes protégées

```http
GET http://localhost:8080/api/utilisateurs
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

---

## 5. Structure des packages

```
com.carrefourconnect/
├── config/
│   └── SecurityConfig.java         ← Configuration Spring Security
├── security/
│   ├── UserDetailsImpl.java        ← Représentation utilisateur pour Security
│   ├── UserDetailsServiceImpl.java ← Chargement BDD
│   ├── JwtUtils.java               ← Gestion des tokens
│   ├── AuthEntryPointJwt.java      ← Gestion des 401
│   └── JwtAuthenticationFilter.java← Filtre HTTP
├── controllers/
│   ├── AuthController.java         ← POST /api/auth/login
│   ├── UtilisateurController.java
│   ├── CommerceController.java
│   └── ... (8 autres contrôleurs)
├── dtos/
│   ├── LoginRequest.java
│   ├── JwtResponse.java
│   └── ... (autres DTOs)
├── entities/
│   ├── Role.java
│   ├── Utilisateur.java
│   ├── Visiteur.java
│   ├── Commercant.java
│   ├── Administrateur.java
│   ├── Commerce.java
│   └── ... (autres entités)
└── ...
```
