# Poste de travail : outils installés

Ce fichier récapitule tous les outils nécessaires au déploiement de CarrefourConnect (Docker, Kubernetes, Helm, CI/CD), avec leur version et les commandes exactes utilisées. Objectif : pouvoir tout réinstaller sur un nouveau PC en suivant ce fichier de haut en bas.

## Machine de référence

| Élément | Valeur |
|---|---|
| Système | Windows 11 Professionnel (build 26200) |
| Processeur | Intel Core 5 210H, 8 cœurs / 12 threads |
| RAM | 16 Go |
| Disque | ~97 Go libres sur `C:` |
| Shell utilisé | **PowerShell** (Windows natif, pas de WSL pour les outils en ligne de commande) |
| Dossier du projet | `C:\dev\CarrefourConnect` (hors OneDrive et Bureau, chemin court) |

> **Pourquoi Windows natif ?** C'est le choix fait au départ du projet : Java et Node étaient déjà installés côté Windows. WSL2 reste activé, car Docker Desktop s'en sert en coulisses pour faire tourner ses conteneurs Linux.
>
> **Admin ?** Sauf mention contraire, toutes les commandes se lancent dans un PowerShell **normal** (pas en administrateur). Si winget a besoin de droits, il ouvre lui-même une fenêtre UAC.

## Récapitulatif

| Outil | Version | Installé par | Étape | Vérification |
|---|---|---|---|---|
| winget | 1.29.380 | fourni avec Windows | — | `winget --version` |
| WSL | 3.0.3.0 (noyau 6.18.54.1) | déjà présent | — | `wsl --version` |
| Git | 2.56.1 | `winget install --id Git.Git -e --source winget` | Préliminaire | `git --version` |
| GitHub CLI (`gh`) | 2.105.0 | `winget install --id GitHub.cli -e` | Préliminaire | `gh --version` |
| VS Code | 1.147.0 | déjà présent | Préliminaire | `code --version` |
| JDK | Oracle JDK 25 (LTS) | déjà présent (`JAVA_HOME=C:\Program Files\Java\jdk-25`) | 0 | `java -version` |
| Maven | 3.9.12 | wrapper du projet (`backend\mvnw.cmd`), rien à installer | 0 | `.\mvnw.cmd -v` |
| Node.js / npm | 24.22.3 / 11.19.1 | `winget install --id OpenJS.NodeJS.24 -e --source winget` | 0 | `node --version` |
| Angular CLI | 21.x | dépendance du projet (`npx ng`), pas d'installation globale | 0 | `npx ng version` |
| Docker Desktop | 29.9.3 (moteur WSL2) | déjà présent (sinon `winget install --id Docker.DockerDesktop -e`) | 1 | `docker run --rm hello-world` |
| Docker Compose | v5.7.0 | inclus dans Docker Desktop (`docker compose`, sans tiret) | 1 | `docker compose version` |
| kubectl | 1.36.1 (client) | fourni par Docker Desktop (sinon `winget install --id Kubernetes.kubectl -e`) | 2 | `kubectl version --client` |
| k3d | 5.9.0 | `winget install --id k3d.k3d -e --source winget` | 2 | `k3d version` |
| Helm | 4.3.2 | `winget install --id Helm.Helm -e --source winget` | 2 | `helm version` |
| k3s (dans le cluster) | 1.36.6 (figé dans `cluster-up.ps1`) | image `rancher/k3s:v1.36.6-k3s1` | 2 | `kubectl get nodes` |

---

## Étape préliminaire

### 1. Git

```powershell
# Vérifier la version installée
git --version
# Installer / mettre à jour (l'ancienne version 2.17.1 datait de 2018)
winget install --id Git.Git -e --source winget
# Fermer et rouvrir PowerShell, puis :
git --version          # attendu : git version 2.56.x.windows.x
```

Configuration globale :

```powershell
git config --global user.name "Fotso12"
git config --global user.email "<ton email GitHub>"
git config --global core.autocrlf input      # garde les fins de ligne LF (scripts et mvnw exécutés sous Linux dans Docker)
git config --global core.longpaths true      # évite les erreurs de chemins trop longs (node_modules)
git config --global init.defaultBranch main
```

Le dépôt contient aussi un `backend/.gitattributes` qui force `mvnw` en LF et les `.cmd` en CRLF, quelle que soit la config locale.

### 2. Authentification GitHub (clé SSH + GitHub CLI)

```powershell
# Tester la clé SSH existante (~/.ssh/id_ed25519)
ssh -T git@github.com                 # attendu : "Hi Fotso12! You've successfully authenticated..."

# Si pas de clé : en créer une, puis l'ajouter sur GitHub (Settings > SSH and GPG keys)
ssh-keygen -t ed25519 -C "<ton email GitHub>"
Get-Content $env:USERPROFILE\.ssh\id_ed25519.pub | Set-Clipboard

# GitHub CLI (servira à suivre le pipeline à l'étape 5)
winget install --id GitHub.cli -e
gh auth login                         # GitHub.com > Login with a web browser > code à 8 caractères
gh config set git_protocol ssh        # la question du protocole n'est pas posée par gh 2.105
gh auth status                        # attendu : Logged in ... / Git operations protocol: ssh
```

### 3. Extensions VS Code

```powershell
code --install-extension vscjava.vscode-java-pack                    # Java (Maven, debug, tests)
code --install-extension vmware.vscode-boot-dev-pack                 # Spring Boot
code --install-extension angular.ng-template                         # Angular
code --install-extension redhat.vscode-yaml                          # YAML (validation des manifests)
code --install-extension ms-kubernetes-tools.vscode-kubernetes-tools # Kubernetes
code --install-extension ms-azuretools.vscode-docker                 # Docker / conteneurs
code --list-extensions                                               # vérification
```

### 4. Cloner le dépôt et créer la branche

```powershell
New-Item -ItemType Directory -Force C:\dev
cd C:\dev
git clone git@github.com:Fotso12/CarrefourConnect.git
cd C:\dev\CarrefourConnect
git switch -c feat/kubernetes
git push -u origin feat/kubernetes    # attendu : branch 'feat/kubernetes' set up to track 'origin/feat/kubernetes'
```

---

## Étape 0 : JDK, Maven, Node.js

### Versions compatibles (vérifiées dans la documentation officielle)

| Composant | Exigence officielle | Source |
|---|---|---|
| Spring Boot 4.2.0 | Java 17 à 27, Maven 3.6.3+ | docs.spring.io/spring-boot/system-requirements.html |
| Angular 21 | Node `^20.19.0 \|\| ^22.12.0 \|\| ^24.0.0`, TypeScript 5.9 | angular.dev/reference/versions |

> **Node 26 n'est PAS supporté par Angular 21** (il l'est par Angular 22). On utilise Node 24, la même version que l'image Docker `node:24-alpine` et que la CI.

### 1. JDK 25

```powershell
java -version                       # attendu : java version "25" ... LTS
$env:JAVA_HOME                      # attendu : C:\Program Files\Java\jdk-25
```

S'il fallait le réinstaller, la distribution Temurin (la même que dans les images Docker) :

```powershell
winget install --id EclipseAdoptium.Temurin.25.JDK -e --source winget
```

Le JDK 17 (`C:\Program Files\Java\jdk-17`) n'a servi qu'à mesurer la base de référence avant migration. Pour lancer une commande ponctuellement avec un autre JDK, sans toucher à la configuration globale :

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'; $env:Path="$env:JAVA_HOME\bin;$env:Path"
```

### 2. Maven (via le wrapper, rien à installer)

```powershell
cd C:\dev\CarrefourConnect\backend
.\mvnw.cmd -v                                         # télécharge Maven 3.9.12 au premier lancement
.\mvnw.cmd test "-Dspring.profiles.active=test"       # tests backend (H2 en mémoire)
```

### 3. Node.js 24

```powershell
node --version                                        # si v26.x : désinstaller d'abord
winget uninstall --id OpenJS.NodeJS.LTS -e            # Node 26 occupe le même dossier que Node 24
winget install --id OpenJS.NodeJS.24 -e --source winget
# fermer / rouvrir PowerShell
node --version                                        # attendu : v24.22.x
```

### 4. Build et tests du frontend

```powershell
cd C:\dev\CarrefourConnect\frontend-web
npm ci --legacy-peer-deps            # installe exactement les versions de package-lock.json
npx ng build                         # build de production -> dist\carrefourconnect-web\browser
npx ng test --watch=false            # tests unitaires (Vitest)
```

---

## Étape 1 : Docker Desktop et Docker Compose

### 1. Mémoire et CPU alloués à WSL2 (donc à Docker)

Avec le moteur WSL2, Docker Desktop n'a pas de réglage de mémoire : c'est le fichier `%USERPROFILE%\.wslconfig` qui décide.

```powershell
notepad $env:USERPROFILE\.wslconfig
```

```ini
[wsl2]
memory=8GB          # la moitié des 16 Go du PC
processors=6        # 6 threads sur 12
swap=4GB

[experimental]
autoMemoryReclaim=gradual   # rend à Windows la mémoire inutilisée
```

```powershell
wsl --shutdown      # obligatoire pour relire le fichier, puis redémarrer Docker Desktop
```

### 2. Démarrer et vérifier Docker

Lancer **Docker Desktop** (menu Démarrer), attendre « Engine running », puis :

```powershell
docker info --format "{{.ServerVersion}} | {{.OperatingSystem}} | {{.NCPU}} CPU | {{.MemTotal}} octets"
# attendu : 29.9.3 | Docker Desktop | 6 CPU | ~8 300 000 000 octets
docker run --rm hello-world          # attendu : "Hello from Docker!"
docker compose version               # attendu : Docker Compose version v5.7.0
```

Réglages conseillés (Settings > General) : « Use the WSL 2 based engine » coché ; « Start Docker Desktop when you sign in » au choix.

### 3. Lancer l'application

Voir le [README](../README.md#lancer-lapplication-en-local-avec-docker-compose). Mémoire constatée avec la stack démarrée : backend ~410 Mo (limite 768 Mo), PostGIS ~65 Mo, Nginx ~7 Mo.

---

## Étape 2 : kubectl, k3d, Helm et cluster local

### 1. Installer et vérifier

```powershell
winget install --id k3d.k3d -e --source winget
winget install --id Helm.Helm -e --source winget
# fermer / rouvrir PowerShell
k3d version                 # attendu : k3d version v5.9.0
helm version                # attendu : Version:"v4.3.2"
kubectl version --client    # attendu : Client Version: v1.36.1 (celui de Docker Desktop)
where.exe kubectl           # un seul kubectl, celui de Docker Desktop
```

> kubectl et le cluster doivent avoir au plus **une version mineure d'écart** (politique de Kubernetes) : on fige donc k3s en 1.36 pour un kubectl 1.36.

### 2. Créer, arrêter, supprimer le cluster

```powershell
.\deploy\scripts\cluster-up.ps1               # crée (ou redémarre) le cluster : ~1 minute
kubectl get nodes                             # attendu : 2 nœuds "Ready" (server-0 et agent-0)
.\deploy\scripts\cluster-down.ps1 -StopOnly   # arrête les nœuds, garde les données (libère la RAM)
.\deploy\scripts\cluster-down.ps1             # supprime tout le cluster
```

Options de `cluster-up.ps1` : `-Agents 0` (un seul nœud, moins de RAM), `-HttpPort 8081` (si le port 80 est pris), `-K3sImage rancher/k3s:<version>`.

Mémoire constatée cluster vide : nœud server ~680 Mo, nœud agent ~180 Mo, load balancer ~9 Mo.

### 3. Ce que k3s installe tout seul (namespace `kube-system`)

| Composant | Rôle |
|---|---|
| Traefik | Ingress Controller : reçoit le trafic de http://localhost (404 tant qu'aucun Ingress n'existe) |
| CoreDNS | DNS interne : un pod joint un Service par son nom (`backend`, `postgis`...) |
| metrics-server | CPU/RAM des pods (`kubectl top`), nécessaire au HPA |
| local-path-provisioner | Crée les volumes persistants (PVC) sur le disque du nœud |

---

## Étape 3 : manifests Kubernetes

Aucun nouvel outil. Commandes de déploiement, exercices et diagnostic : [deploy/k8s/README.md](k8s/README.md).

Mémoire constatée avec l'application déployée : backend ~380 Mo par pod (x2), PostGIS ~75 Mo, frontend ~6 Mo, plus ~870 Mo pour le cluster lui-même.

---

## Étape 4 : chart Helm

Aucun nouvel outil (Helm 4.3.2 installé à l'étape 2). Commandes : [deploy/helm/carrefourconnect/README.md](helm/carrefourconnect/README.md).

---

## Erreurs rencontrées et solutions

| Symptôme | Cause | Solution |
|---|---|---|
| `gh : Le terme «gh» n'est pas reconnu...` juste après l'installation | La fenêtre PowerShell était ouverte avant l'installation : le PATH est lu à l'ouverture du terminal | Fermer et rouvrir PowerShell (et VS Code entièrement s'il s'agit de son terminal intégré), ou recharger le PATH : `$env:Path = [Environment]::GetEnvironmentVariable('Path','Machine') + ';' + [Environment]::GetEnvironmentVariable('Path','User')` |
| `git --version` affiche toujours l'ancienne version | Même problème de PATH, ou deux Git installés | Rouvrir le terminal ; `where.exe git` pour voir lequel passe en premier ; désinstaller l'ancien |
| `fatal: a branch named 'feat/kubernetes' already exists` | La branche avait déjà été créée | Rien à faire : `git switch feat/kubernetes` pour s'y placer |
| `Permission denied (publickey)` avec `ssh -T` | Clé SSH absente de GitHub | Copier `id_ed25519.pub` dans GitHub > Settings > SSH and GPG keys |
| `Fatal error compiling: java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN` | Ancienne version de Lombok (celle de Spring Boot 3.4) incompatible avec le compilateur de Java 25 | Compiler avec le JDK 17, ou mettre à jour Lombok (fait par la migration vers Spring Boot 4.2) |
| `'mvnw.cmd' n'est pas reconnu` | Commande lancée hors du dossier `backend`, ou sans `.\` devant | `cd backend` puis `.\mvnw.cmd ...` |
| `failed to connect to the docker API at npipe:////./pipe/dockerDesktopLinuxEngine` | Docker Desktop n'est pas démarré | Lancer Docker Desktop et attendre « Engine running » |
| `docker info` affiche 12 CPU / 16 Go | `.wslconfig` pas relu | `wsl --shutdown` puis redémarrer Docker Desktop |
| `required variable JWT_SECRET is missing a value` au `docker compose up` | `deploy/.env` absent ou incomplet | `Copy-Item deploy\.env.example deploy\.env` puis le remplir |
| `bind: address already in use` sur 8080 | Un autre programme utilise le port | `Get-NetTCPConnection -LocalPort 8080` pour le trouver, ou changer `"8080:80"` dans le compose |
| `k3d` / `helm` « n'est pas reconnu » juste après winget | PATH lu à l'ouverture du terminal | Rouvrir PowerShell (winget l'indique : « redémarrez votre interpréteur de commandes ») |
| `Le port 80 est déjà utilisé` (cluster-up) | IIS, Skype, un autre serveur web... | `Get-NetTCPConnection -LocalPort 80 -State Listen` pour trouver le processus, ou `cluster-up.ps1 -HttpPort 8081` |
| `http://localhost` répond 404 | Normal tant qu'aucun Ingress n'est déployé : c'est Traefik qui répond | — |
| `The connection to the server ... was refused` (kubectl) | Cluster arrêté ou mauvais contexte | `.\deploy\scripts\cluster-up.ps1`, puis `kubectl config current-context` (attendu : `k3d-carrefourconnect`) |
| Pods backend bloqués en `Init:0/1`, logs « PostGIS pas encore prêt... » alors que postgis-0 est prêt | `pg_isready` lancé avec l'UID 1001, inconnu dans l'image PostGIS : sans `-U`, il abandonne (code 3 « no attempt ») | Passer `-U "$POSTGRES_USER"` à `pg_isready` (corrigé dans `30-backend.yaml`) |
| `ErrImagePull` / `ImagePullBackOff` sur les pods | Image locale non importée dans le cluster | `k3d image import <image>:<tag> -c carrefourconnect` |
| `kubectl apply --dry-run=server` : `namespaces "carrefourconnect" not found` | Un dry-run ne crée rien, donc le namespace n'existe pas pour les autres objets | Créer d'abord le namespace (`kubectl apply -f deploy/k8s/00-namespace.yaml`) |
| `UPGRADE FAILED: resource Deployment/... not ready` | Un pod de la nouvelle révision ne devient jamais prêt | `kubectl get pods`, puis `kubectl logs <pod> --previous` ; revenir en arrière avec `helm rollback <release> <révision>` |
| Nouveau pod backend en `CrashLoopBackOff` : `Query did not return a unique result: 2 results` | Doublons créés par deux pods initialisant la base en même temps (corrigé : contraintes uniques) | Repartir d'une base propre : `helm uninstall`, `kubectl -n carrefourconnect delete pvc --all`, `helm install` |
| Tests Maven : `Unresolved compilation problem` / `declared package ... does not match` | L'extension Java de VS Code (compilateur Eclipse) écrit aussi dans `target/` | `.\mvnw.cmd clean test` (et package des tests aligné sur leur dossier) |
| `secrets.jwtSecret est obligatoire` au `helm install` | Fichier `values-secret.yaml` absent ou non passé avec `-f` | Le créer depuis `values-secret.example.yaml` et l'ajouter à la commande |
| `npm warn install-scripts ... not yet covered by allowScripts` | Nouveauté de npm 11 : il demande d'approuver les scripts d'installation | Sans effet sur le build ici ; à examiner avec `npm install-scripts ls` |
