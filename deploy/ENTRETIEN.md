# Fiche de révision : Kubernetes et DevOps dans CarrefourConnect

Chaque concept en 3 lignes : **ce que c'est**, **à quoi ça sert**, **où c'est dans CarrefourConnect**. Puis 10 questions d'entretien probables avec des éléments de réponse, appuyés sur ce projet.

## Le pitch en 30 secondes

> « J'ai conteneurisé CarrefourConnect, un annuaire de commerces en Spring Boot 4 / Angular 21 / PostGIS, puis je l'ai déployé sur Kubernetes avec un chart Helm que j'ai écrit : 2 à 4 pods backend avec autoscaling, mises à jour sans coupure grâce aux sondes Actuator, base PostGIS en StatefulSet, Ingress Traefik. Le pipeline GitHub Actions teste le code, publie les images sur GHCR, les scanne avec Trivy, et surtout déploie l'application sur un cluster kind jetable à chaque push : si elle ne démarre pas vraiment dans Kubernetes, le pipeline échoue. La supervision passe par Prometheus et Grafana, avec un dashboard versionné. Le projet m'a fait corriger de vrais bugs que seul Kubernetes révélait, comme une condition de course au démarrage de plusieurs pods. »

---

## Les concepts

### Conteneur / image
- Une **image** empaquette l'application et tout ce dont elle a besoin (JRE, bibliothèques) ; un **conteneur** est une image en cours d'exécution, isolée.
- Sert à garantir que l'application tourne pareil partout (poste, CI, production).
- Ici : images multi-étapes (`backend/Dockerfile` : JDK pour compiler, JRE 25 pour exécuter, utilisateur non-root 1001, jar découpé en couches ; `frontend-web/Dockerfile` : Node 24 puis Nginx).

### Pod
- La plus petite unité de Kubernetes : un ou plusieurs conteneurs qui partagent le réseau (même IP) et des volumes.
- Éphémère : un pod peut être tué et remplacé à tout moment, avec une nouvelle IP.
- Ici : chaque pod backend contient le conteneur Spring Boot et un **initContainer** `attendre-postgis` qui bloque le démarrage tant que la base n'accepte pas de connexions.

### Deployment
- Décrit l'état voulu de pods **sans état et interchangeables** (image, nombre de réplicas) ; un contrôleur le maintient en permanence.
- Gère les mises à jour progressives et les retours arrière (via des ReplicaSets successifs).
- Ici : `carrefourconnect-backend` (2 à 4 réplicas, HPA) et `carrefourconnect-frontend` (Nginx). Un pod supprimé est recréé en ~20 s sous un autre nom.

### StatefulSet
- Comme un Deployment, mais pour des applications **avec état** : noms stables (`postgis-0`), création ordonnée, un volume persistant **par pod** qui le suit.
- Indispensable pour une base de données, dont l'identité et les données ne sont pas interchangeables.
- Ici : PostgreSQL/PostGIS, avec `volumeClaimTemplates` (2 Gi). Supprimer `postgis-0` le recrée avec le même nom et les mêmes données.

### Service
- Une adresse **stable** (nom DNS + IP virtuelle) devant un groupe de pods choisis par labels ; répartit le trafic entre les pods **prêts**.
- `ClusterIP` (interne), `NodePort`/`LoadBalancer` (exposition), `headless` (`clusterIP: None`, le DNS renvoie directement les IP des pods).
- Ici : `carrefourconnect-backend` (ClusterIP, sessions sticky pour SockJS), `carrefourconnect-frontend`, et `carrefourconnect-postgis` (headless, exigé par le StatefulSet).

### Ingress
- Règles de routage HTTP de l'extérieur vers les Services (par chemin ou nom d'hôte), appliquées par un **Ingress Controller**.
- Permet une seule adresse publique pour plusieurs services, et la terminaison TLS.
- Ici : Traefik (fourni par k3s) ; `/api`, `/ws` (WebSocket, transmise nativement) et `/uploads` vers le backend, `/` vers le frontend. `/actuator` volontairement non routé.

### ConfigMap
- Configuration **non sensible** sous forme de clés/valeurs, injectée en variables d'environnement ou en fichiers.
- Sépare la configuration de l'image : une seule image pour tous les environnements.
- Ici : URL JDBC, hôte SMTP, URL publique. Une empreinte (`checksum/config`) dans le modèle de pod relance les pods quand elle change.

### Secret
- Comme une ConfigMap, pour les données **sensibles** ; encodé en base64 (pas chiffré par défaut), protégé par les droits d'accès (RBAC).
- En production : chiffrement au repos et/ou gestionnaire externe (Sealed Secrets, External Secrets + Vault).
- Ici : mot de passe PostgreSQL, clé JWT (≥ 64 caractères, sinon le chart refuse de s'installer), clés Mailjet/Cloudinary. Jamais dans Git : uniquement `values-secret.example.yaml`.

### Sondes (probes)
- **startup** : « as-tu fini de démarrer ? » (suspend les autres). **liveness** : « es-tu vivant ? », sinon redémarrage. **readiness** : « peux-tu recevoir du trafic ? », sinon retrait du Service, sans redémarrage.
- Elles rendent les mises à jour sans coupure et l'autoréparation fiables.
- Ici : Spring Boot Actuator `/actuator/health/liveness` (sans la base) et `/actuator/health/readiness` (avec la base) ; startupProbe jusqu'à 3 min car Spring met ~25 s à démarrer.

### Rolling update
- Mise à jour progressive : de nouveaux pods remplacent les anciens petit à petit, selon `maxSurge` et `maxUnavailable`.
- Permet de déployer sans interruption de service, et de revenir en arrière (`kubectl rollout undo`, `helm rollback`).
- Ici : `maxSurge: 1`, `maxUnavailable: 0`, readiness + `preStop` de 5 s : mesuré à **410 requêtes / 410 réponses 200** pendant une mise à jour.

### Helm
- Gestionnaire de paquets de Kubernetes : un **chart** = des modèles YAML paramétrés par des fichiers de **valeurs** ; une installation = une **release** avec des **révisions**.
- Un seul chart pour tous les environnements ; `upgrade`, `history`, `rollback` de toute l'application d'un coup.
- Ici : `deploy/helm/carrefourconnect` (labels `app.kubernetes.io/*`, `values-dev.yaml` pour k3d, `helm test`), publié en pièce jointe des releases GitHub.

### HPA (HorizontalPodAutoscaler)
- Ajuste automatiquement le nombre de pods selon une métrique (ici le CPU, lu par metrics-server) : `réplicas = ceil(actuels × utilisation / cible)`.
- Absorbe les pics de charge sans surdimensionner en permanence.
- Ici : 2 à 4 pods backend, cible 70 % de la request CPU (175m). Sous le test k6, passage de 2 à 4 pods en 15 s ; descente prudente après 2 min de calme.

### Prometheus
- Base de données de **séries temporelles** qui va chercher (« scrape ») les métriques des applications à intervalle régulier ; requêtes en **PromQL**.
- Le Prometheus Operator le configure via des objets Kubernetes (**ServiceMonitor**).
- Ici : backend instrumenté par Micrometer (`/actuator/prometheus`), collecté toutes les 15 s grâce au ServiceMonitor du chart ; rétention 24 h ; installé avec kube-prometheus-stack.

### Grafana
- Outil de **tableaux de bord** qui interroge Prometheus (et d'autres sources).
- Dashboards en JSON, donc versionnables et chargeables automatiquement.
- Ici : dashboard « CarrefourConnect - Backend » (débit, latence p95, erreurs 5xx, CPU vs cible HPA, heap JVM, pauses GC, réplicas) livré par le chart dans une ConfigMap, accessible par `port-forward` uniquement.

### CI/CD
- **CI** : chaque modification est automatiquement construite et testée. **CD** : ce qui passe est automatiquement livré (images, release).
- Détecte les régressions tôt et rend les livraisons reproductibles.
- Ici : GitHub Actions ; tests, **déploiement réel sur un cluster kind**, images GHCR (SHA, version, latest), scan Trivy, SonarCloud, release sur tag `vX.Y.Z`.

---

## 10 questions d'entretien probables

### 1. Quelle est la différence entre une liveness et une readiness probe ? Que se passerait-il si on mettait la base de données dans la liveness ?
- Liveness en échec → Kubernetes **redémarre** le conteneur ; readiness en échec → le pod est **retiré du Service**, sans redémarrage.
- Base dans la liveness : une panne de PostgreSQL ferait redémarrer en boucle **tous** les backends, qui ne sont pas en cause, et aggraverait l'incident.
- Dans le projet : la base est dans la readiness seulement. Observé en vrai : en supprimant `postgis-0`, les backends sont sortis du trafic quelques secondes puis y sont revenus seuls, avec 0 redémarrage.

### 2. Pourquoi un StatefulSet pour PostgreSQL et pas un Deployment ?
- Un Deployment suppose des pods interchangeables, avec noms aléatoires ; une base a besoin d'une **identité stable** et de **son** disque.
- Le StatefulSet garantit `postgis-0`, un PVC par pod via `volumeClaimTemplates`, et un ordre de démarrage/arrêt.
- Nuance à savoir dire : ajouter des réplicas à un StatefulSet ne crée pas un cluster PostgreSQL répliqué ; il faudrait un opérateur (CloudNativePG) ou une base managée. D'où l'option `postgis.enabled=false` + `externalDatabase.url` dans le chart.

### 3. Comment déployez-vous une nouvelle version sans interruption de service ?
- Rolling update avec `maxSurge: 1` / `maxUnavailable: 0` : le nouveau pod doit être **prêt** (readiness) avant qu'un ancien soit arrêté.
- Arrêt propre : `preStop` de 5 s (le temps que le pod sorte des endpoints du Service et de l'Ingress), puis arrêt « graceful » de Spring qui termine les requêtes en cours.
- Preuve mesurée : 410 requêtes envoyées pendant un rolling update, 410 réponses 200. Retour arrière : `helm rollback`.

### 4. Comment gérez-vous les secrets ? Un Secret Kubernetes est-il sûr ?
- Jamais dans Git : fichiers `*.example` versionnés, vrais fichiers ignorés ; en CI, secrets factices générés à chaque exécution et masqués dans les logs.
- Un Secret n'est qu'**encodé en base64** : sa sécurité repose sur le RBAC ; en production on ajoute le chiffrement au repos (etcd) et un gestionnaire externe. Le chart accepte un `existingSecret` pour ça.
- Retour d'expérience honnête : une clé JWT par défaut était codée en dur dans le code et figure dans l'historique Git ; je l'ai retirée, rendue obligatoire via `JWT_SECRET`, et considérée comme compromise plutôt que de réécrire l'historique d'un dépôt public.

### 5. Racontez un bug que vous avez rencontré en passant à Kubernetes.
- Au premier `helm install` avec 2 réplicas, les deux pods ont exécuté en même temps l'initialisation des données de référence : « si le rôle ADMIN n'existe pas, l'insérer ». Les deux ont vu « absent » et inséré : **condition de course**, 2 rôles ADMIN.
- Symptôme : le `helm upgrade` suivant échouait, chaque nouveau pod plantait (`Query did not return a unique result`). Diagnostic : `kubectl logs --previous`, puis requête SQL dans le pod PostGIS. Remise en état immédiate : `helm rollback`.
- Correctif : contraintes uniques en base (une seule insertion peut réussir) et initialisation qui tolère l'erreur de doublon, plus des tests. Vérifié en vrai : le second pod a logué « déjà créé par une autre instance, on continue ». La vraie solution long terme : des migrations versionnées (Flyway) qui verrouillent la base pendant la migration.
- Autres exemples : images en erreur 500 sous Linux (URL `file:/` + chemin absolu → `file://app/...`, « app » lu comme un nom d'hôte) ; `pg_isready` qui abandonnait car le pod tournait avec un UID inexistant dans l'image.

### 6. À quoi sert Helm par rapport à de simples fichiers YAML ?
- Paramétrage : un même chart pour k3d, la CI (kind) et un cloud ; seules les valeurs changent (images locales en dev, GHCR ailleurs, HPA désactivé en CI).
- Cycle de vie : une release versionnée (`helm history`), retour arrière de toute l'application d'un coup (`helm rollback`), désinstallation propre, `helm test`.
- Bonnes pratiques appliquées : labels `app.kubernetes.io/*`, sélecteurs stables (immuables dans un Deployment), empreintes de configuration pour relancer les pods, `required`/`fail` pour refuser une installation incomplète.

### 7. Comment le HPA décide-t-il d'ajouter des pods ? Quelles conditions faut-il réunir ?
- Il lit l'utilisation CPU via metrics-server toutes les 15 s et calcule `ceil(réplicas × utilisation moyenne / cible)`, borné par min et max.
- Prérequis : metrics-server, et des **requests CPU** sur le conteneur (l'utilisation est un % de la request). Avec un HPA, on ne fixe plus `replicas` dans le Deployment, sinon chaque déploiement le contredit.
- Dans le projet : cible 70 % de 250m ; sous 60 utilisateurs virtuels k6, CPU à 137 % de la cible → 4 pods en 15 s, puis le CPU par pod est redescendu vers la cible ; scale-down retardé de 2 min pour éviter le « yo-yo ».

### 8. Que fait votre pipeline CI/CD, et comment savez-vous qu'un déploiement fonctionne vraiment ?
- Tests unitaires et d'intégration (Maven, Angular, Flutter), puis un job qui **crée un cluster kind dans le runner**, installe le chart avec des secrets factices, attend que tous les pods soient prêts (`kubectl wait`), appelle `/actuator/health` et lance `helm test`. En cas d'échec, il affiche events, `describe` et logs.
- Sur `main` : images GHCR taguées avec le SHA court (immuable, traçable) et `latest` ; Trivy bloque sur une vulnérabilité CRITICAL corrigeable. Sur tag : release avec le chart.
- Sécurité du pipeline : `GITHUB_TOKEN` éphémère avec permissions minimales, actions tierces épinglées par SHA. Leçon apprise : **figer les versions des outils** (le job APK a cassé du jour au lendemain parce que le runner est passé à Java 25 et que `channel: stable` installait un Flutter trop récent).

### 9. Comment supervisez-vous l'application ? Quelles métriques regardez-vous en premier ?
- Prometheus collecte `/actuator/prometheus` (Micrometer) via un ServiceMonitor ; Grafana affiche un dashboard versionné dans le chart.
- Les « signaux d'or » : **trafic** (requêtes/s), **latence** (p95, plus parlante que la moyenne), **erreurs** (taux de 5xx), **saturation** (CPU par rapport à la cible du HPA, heap JVM, pauses GC).
- Exemple de lecture : une heap en dents de scie est normale (le GC libère) ; un plancher qui monte sans cesse signale une fuite mémoire. Mesuré sous charge : 234 req/s, p95 22 ms, 0 erreur.

### 10. Votre application a des WebSockets et des fichiers uploadés : quelles difficultés avec plusieurs réplicas ?
- **Fichiers** : stockés sur le disque du pod, ils seraient invisibles des autres pods et perdus à la recréation. Solution retenue : un volume `ReadWriteOnce` partagé ; il épingle tous les pods sur un nœud (limite assumée). En production : stockage objet (S3/MinIO) pour un backend vraiment sans état.
- **WebSocket** : le broker STOMP simple de Spring vit dans la mémoire de chaque pod, donc une notification émise sur le pod A n'atteint que les clients du pod A. Solution : broker partagé (Redis pub/sub ou RabbitMQ). Les modes de repli de SockJS exigent aussi des **sessions sticky** (cookie Traefik sur le Service).
- Ce qu'il faut retenir : passer à plusieurs réplicas oblige à sortir l'état des pods (sessions, fichiers, caches, messages) vers des services partagés.

---

## Chiffres à retenir

| Mesure | Valeur |
|---|---|
| Tests backend | 87 |
| Rolling update sous trafic | 410 requêtes, 410 réponses 200 |
| Recréation d'un pod backend supprimé | ~20 s |
| Démarrage de Spring Boot dans un pod | ~25 s |
| Test de charge k6 | 48 740 requêtes, 0 % d'erreur, p95 24 ms, pic 234 req/s |
| HPA | 2 → 4 pods en 15 s (CPU à 137 % de la cible) |
| Installation complète `helm install` | ~40 s |
| Supervision (kube-prometheus-stack) | ~1 Go de RAM |
