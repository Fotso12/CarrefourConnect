# Journal des modifications

Toutes les évolutions notables du projet sont consignées ici.
Format inspiré de [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/), numérotation [SemVer](https://semver.org/lang/fr/) : `MAJEUR.MINEUR.CORRECTIF`.
Tant que la version commence par `0.`, l'application n'est pas considérée comme stable en production.

## [0.3.0] - 2026-11-23

Supervision, autoscaling et documentation du déploiement.

### Ajouté
- **Supervision** avec kube-prometheus-stack (`deploy/monitoring/`) : Prometheus (rétention 24 h), Grafana, Alertmanager, kube-state-metrics, node-exporter, ressources limitées pour un PC.
- Chart Helm : **ServiceMonitor** du backend (créé seulement si le Prometheus Operator est présent), **dashboard Grafana** versionné et chargé automatiquement (débit, latence p95, erreurs 5xx, CPU, heap JVM, pauses GC, réplicas), **HorizontalPodAutoscaler** activable (`backend.autoscaling`).
- **Test de charge k6** (`deploy/load-test/charge.js`) sur les endpoints publics, lançable via Docker sans installation.
- Documentation : `deploy/README.md` (architecture, démarrage Compose puis k3d + Helm, Grafana, pipeline, dépannage), section « Déploiement Kubernetes » du README, emplacement des captures (`docs/captures/`), fiche de révision `deploy/ENTRETIEN.md`.

### Modifié
- Pipeline : APK construit avec Java 17 et Flutter figé en 3.38.10 (version du projet) ; job APK lançable à la demande.

## [0.2.0] - 2026-11-23

Mise en production conteneurisée : Docker Compose, Kubernetes, Helm, CI/CD et socle de supervision.

### Ajouté
- **Docker Compose** (`deploy/docker-compose.yml`) : PostGIS avec volume persistant, backend et frontend, healthchecks et démarrage ordonné, Nginx en reverse proxy (`/api`, `/ws`, `/uploads`).
- **Cluster Kubernetes local** avec k3d (scripts `deploy/scripts/cluster-up` et `cluster-down`, PowerShell et bash).
- **Manifests Kubernetes** (`deploy/k8s/`) : StatefulSet PostGIS, Deployment backend à 2 réplicas avec sondes startup/liveness/readiness et rolling update sans coupure, frontend, Ingress Traefik.
- **Chart Helm** `carrefourconnect` (`deploy/helm/`) : templates paramétrés, labels standards `app.kubernetes.io/*`, valeurs par environnement, secrets hors du dépôt, `helm test`.
- **Pipeline CI/CD** GitHub Actions (`.github/workflows/ci-cd.yml`) : tests, test de déploiement sur un cluster kind, images publiées sur GHCR (tags SHA, `latest` et version), scan Trivy, SonarCloud, APK, et release automatique sur tag `v*`.
- **Spring Boot Actuator** : `/actuator/health` (groupes liveness et readiness) et `/actuator/prometheus`.
- Documentation : `deploy/SETUP.md` (installation du poste), `deploy/CI-CD.md`, guides des manifests et du chart, `CHANGELOG.md`.

### Modifié
- Backend migré vers **Java 25** et **Spring Boot 4.2.0** (Spring Security 7, Jackson 3, springdoc 3).
- Image Docker du backend : JRE 25, utilisateur non-root, options JVM adaptées aux conteneurs, jar découpé en couches, logs sur la sortie standard.
- Frontend : appels à l'API en **chemins relatifs** en production (même domaine), Node 24 pour le build.
- La clé de signature JWT vient obligatoirement de la variable `JWT_SECRET`.

### Corrigé
- Images envoyées introuvables (erreur 500) une fois le backend exécuté sous Linux : URL `file://` mal construite.
- Doublons de données de référence (rôle ADMIN) quand plusieurs instances du backend démarrent en même temps : contraintes uniques en base.
- Application mobile : erreur de type quand le GPS ne répond pas dans le délai.
- Tests existants en échec (contexte Spring, test de création d'offre) ; couverture de code du frontend jamais transmise à SonarCloud.

### Supprimé
- Spring Boot Admin Server (aucune application ne s'y enregistrait ; remplacé par Actuator + Prometheus).
- Clé JWT par défaut codée en dur dans le code (elle figure dans l'historique Git : considérée comme compromise).
- Dossiers `backend/logs/` et `backend/uploads/` du suivi Git ; ancien workflow `backend/.github/` jamais exécuté.

### Sécurité
- Aucun secret dans le dépôt : fichiers `*.example` uniquement (`.env.example`, `secret.example.yaml`, `values-secret.example.yaml`).
- Pods backend non-root, système de fichiers en lecture seule, capabilities supprimées ; actions GitHub tierces épinglées par SHA ; permissions minimales du jeton CI.

### Limites connues
- Notifications temps réel : le broker STOMP de Spring est propre à chaque pod (une notification n'atteint en direct que les clients du même pod).
- Images envoyées sur un volume `ReadWriteOnce` : tous les pods backend sont placés sur le même nœud.
- Schéma de base géré par `ddl-auto=update` (pas de migrations versionnées).

## [0.1.0] - 2026-05-23

Version initiale de l'application, avant le travail de conteneurisation : API Spring Boot (Java 17, Spring Boot 3.4), frontend Angular 21, application mobile Flutter.

[0.3.0]: https://github.com/Fotso12/CarrefourConnect/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/Fotso12/CarrefourConnect/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/Fotso12/CarrefourConnect/releases/tag/v0.1.0
