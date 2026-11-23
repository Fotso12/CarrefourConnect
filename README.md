# CarrefourConnect

[![CI/CD](https://github.com/Fotso12/CarrefourConnect/actions/workflows/ci-cd.yml/badge.svg?branch=main)](https://github.com/Fotso12/CarrefourConnect/actions/workflows/ci-cd.yml)

Annuaire numérique interactif des commerces : recherche par catégorie et par proximité (carte), fiches commerces, avis, offres, abonnements des commerçants et notifications en temps réel.

| Module | Technologies |
|---|---|
| [`backend/`](backend) | Java 25, Spring Boot 4.2, Spring Security (JWT), JPA/Hibernate, PostgreSQL + PostGIS, WebSocket STOMP |
| [`frontend-web/`](frontend-web) | Angular 21, Tailwind CSS, Leaflet |
| [`frontend-mobile/`](frontend-mobile) | Flutter |
| [`deploy/`](deploy) | Docker Compose, Kubernetes, Helm, supervision |

## Lancer l'application en local avec Docker Compose

Prérequis : Docker Desktop démarré (voir [deploy/SETUP.md](deploy/SETUP.md)).

```powershell
# 1. Une seule fois : créer le fichier de configuration local (ignoré par Git)
Copy-Item deploy\.env.example deploy\.env
#    puis éditer deploy\.env : au minimum POSTGRES_PASSWORD et JWT_SECRET (64 caractères min.)

# 2. Construire les images et démarrer la base, le backend et le frontend
docker compose -f deploy/docker-compose.yml up -d --build

# 3. Suivre l'état (les 3 services doivent passer "healthy" en ~1 minute)
docker compose -f deploy/docker-compose.yml ps
```

| URL | Contenu |
|---|---|
| http://localhost:8080 | Application web (Nginx : Angular + reverse proxy `/api`, `/ws`, `/uploads`) |
| http://localhost:8084/swagger-ui/index.html | Documentation de l'API (accès direct au backend, depuis ta machine uniquement) |
| http://localhost:8084/actuator/health | Santé du backend |

```powershell
docker compose -f deploy/docker-compose.yml logs -f backend   # logs du backend
docker compose -f deploy/docker-compose.yml down              # arrêter (les données sont conservées)
docker compose -f deploy/docker-compose.yml down -v           # arrêter ET effacer la base et les uploads
```

## Déploiement Kubernetes

L'application se déploie aussi sur un **cluster Kubernetes local** (k3d) avec un **chart Helm**, supervisée par **Prometheus et Grafana**, et chaque modification est testée par un pipeline **GitHub Actions** qui la déploie sur un cluster jetable.

- 2 à 4 pods backend selon la charge (**HPA**), mises à jour **sans coupure** (rolling update + sondes Actuator)
- PostgreSQL/PostGIS en **StatefulSet** avec volume persistant ; configuration par **ConfigMap** et **Secret**
- **Ingress** Traefik : une seule adresse, `/api` et WebSocket `/ws` vers le backend
- **Redis** : notifications temps réel partagées entre les pods (pub/sub) et vraie déconnexion (liste noire des JWT)
- Images publiées sur **GHCR**, scannées par **Trivy**, releases versionnées (SemVer) avec le chart Helm

```powershell
.\deploy\scripts\cluster-up.ps1
helm install carrefourconnect deploy\helm\carrefourconnect -n carrefourconnect --create-namespace `
  -f deploy\helm\carrefourconnect\values-dev.yaml -f deploy\helm\carrefourconnect\values-secret.yaml --wait
```

➡️ **Guide complet** (architecture, démarrage pas à pas, Grafana, pipeline, dépannage) : **[deploy/README.md](deploy/README.md)**

### Captures

| | |
|---|---|
| ![Pods de l'application dans le cluster](docs/captures/01-pods.png) | ![Pipeline GitHub Actions vert](docs/captures/02-pipeline.png) |
| *`kubectl get pods` : backend, frontend, PostGIS* | *Pipeline CI/CD vert, test de déploiement sur kind inclus* |
| ![Grafana pendant le test de charge](docs/captures/03-grafana-charge.png) | ![Le HPA ajoute des pods](docs/captures/04-hpa.png) |
| *Grafana pendant le test de charge k6* | *Le HPA passe de 2 à 4 pods sous la charge* |

## Documentation

- [deploy/README.md](deploy/README.md) : déploiement (Docker Compose, Kubernetes, Helm, supervision, CI/CD)
- [deploy/SETUP.md](deploy/SETUP.md) : installation du poste de travail (outils, versions, commandes)
- [CHANGELOG.md](CHANGELOG.md) : journal des versions
- [GUIDE_DEMARRAGE.md](GUIDE_DEMARRAGE.md) : structure du projet, modèle de données, création du premier administrateur
- [GUIDE_DEPLOIEMENT.md](GUIDE_DEPLOIEMENT.md) : déploiement
- [architecture_technique.md](architecture_technique.md) : architecture technique
