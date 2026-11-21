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
| JDK | *à valider à l'étape 0* | — | 0 | `java -version` |
| Node.js / npm | *à valider à l'étape 0* | — | 0 | `node --version` |
| Docker Desktop | *à valider à l'étape 1* | — | 1 | `docker run hello-world` |
| kubectl, k3d, Helm | *à installer à l'étape 2* | — | 2 | — |

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

## Erreurs rencontrées et solutions

| Symptôme | Cause | Solution |
|---|---|---|
| `gh : Le terme «gh» n'est pas reconnu...` juste après l'installation | La fenêtre PowerShell était ouverte avant l'installation : le PATH est lu à l'ouverture du terminal | Fermer et rouvrir PowerShell (et VS Code entièrement s'il s'agit de son terminal intégré), ou recharger le PATH : `$env:Path = [Environment]::GetEnvironmentVariable('Path','Machine') + ';' + [Environment]::GetEnvironmentVariable('Path','User')` |
| `git --version` affiche toujours l'ancienne version | Même problème de PATH, ou deux Git installés | Rouvrir le terminal ; `where.exe git` pour voir lequel passe en premier ; désinstaller l'ancien |
| `fatal: a branch named 'feat/kubernetes' already exists` | La branche avait déjà été créée | Rien à faire : `git switch feat/kubernetes` pour s'y placer |
| `Permission denied (publickey)` avec `ssh -T` | Clé SSH absente de GitHub | Copier `id_ed25519.pub` dans GitHub > Settings > SSH and GPG keys |
