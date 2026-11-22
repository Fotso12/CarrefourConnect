# Chart Helm `carrefourconnect`

Déploie toute l'application (PostgreSQL/PostGIS, backend Spring Boot, frontend Angular, Ingress) à partir de **modèles paramétrés**. Les manifests bruts de `deploy/k8s/` restent dans le dépôt comme référence pédagogique ; c'est ce chart qu'on utilise désormais.

```
carrefourconnect/
├── Chart.yaml                    # identité du chart (version du chart, appVersion de l'appli)
├── values.yaml                   # valeurs par défaut (images GHCR, 2 réplicas...)
├── values-dev.yaml               # surcharges pour k3d local (images importées, tag dev)
├── values-secret.example.yaml    # MODÈLE des secrets (le vrai values-secret.yaml est ignoré par Git)
└── templates/
    ├── _helpers.tpl              # fonctions : noms, labels app.kubernetes.io/*, image, URL JDBC...
    ├── configmap.yaml            # configuration non sensible
    ├── secret.yaml               # Secret (sauf si secrets.existingSecret)
    ├── postgis.yaml              # Service headless + StatefulSet (désactivable : postgis.enabled)
    ├── backend.yaml              # PVC uploads + Deployment + Service
    ├── frontend.yaml             # Deployment + Service
    ├── ingress.yaml              # /api, /ws, /uploads -> backend ; / -> frontend
    ├── NOTES.txt                 # message affiché après install/upgrade
    └── tests/test-connexion.yaml # helm test : readiness backend, API, page d'accueil
```

## Valider sans rien déployer

```powershell
$c = "deploy\helm\carrefourconnect"
helm lint $c -f $c\values-dev.yaml -f $c\values-secret.yaml --strict      # syntaxe et bonnes pratiques
helm template carrefourconnect $c -n carrefourconnect -f $c\values-dev.yaml -f $c\values-secret.yaml   # YAML généré
helm template carrefourconnect $c -f $c\values-dev.yaml -f $c\values-secret.yaml --show-only templates/ingress.yaml
```

Garde-fous : sans `secrets.jwtSecret`, ou avec moins de 64 caractères, `helm template`/`install` s'arrêtent avec un message explicite au lieu de déployer une application qui planterait.

## Installer sur k3d

```powershell
$c = "deploy\helm\carrefourconnect"

# 1. Les secrets (une seule fois)
Copy-Item $c\values-secret.example.yaml $c\values-secret.yaml
notepad $c\values-secret.yaml

# 2. Les images locales dans le cluster
docker build -t carrefourconnect-backend:dev backend
docker build -t carrefourconnect-frontend:dev frontend-web
k3d image import carrefourconnect-backend:dev carrefourconnect-frontend:dev -c carrefourconnect

# 3. Installation (attend que tous les pods soient prêts)
helm install carrefourconnect $c -n carrefourconnect --create-namespace `
  -f $c\values-dev.yaml -f $c\values-secret.yaml --wait --timeout 6m

# 4. Vérification
helm test carrefourconnect -n carrefourconnect --logs
```

Application : http://localhost

## Faire vivre la release

Une **release** est une installation du chart ; chaque `install`, `upgrade` ou `rollback` crée une **révision** numérotée, conservée par Helm (dans des Secrets du namespace).

```powershell
# Nouvelle révision : 3 réplicas du backend
helm upgrade carrefourconnect $c -n carrefourconnect -f $c\values-dev.yaml -f $c\values-secret.yaml `
  --set backend.replicaCount=3 --wait --description "3 réplicas backend"

# Nouvelle révision : configuration modifiée -> les pods redémarrent automatiquement
helm upgrade carrefourconnect $c -n carrefourconnect -f $c\values-dev.yaml -f $c\values-secret.yaml `
  --set backend.replicaCount=3 --set config.extraEnv.LOGGING_LEVEL_COM_CARREFOURCONNECT=INFO `
  --wait --description "log applicatif en INFO"

helm history carrefourconnect -n carrefourconnect                 # liste des révisions
helm get values carrefourconnect -n carrefourconnect --revision 2 # valeurs d'une révision
helm rollback carrefourconnect 2 -n carrefourconnect --wait       # revenir à la révision 2 (crée une nouvelle révision)
helm list -n carrefourconnect                                      # releases installées
helm uninstall carrefourconnect -n carrefourconnect                # désinstaller
```

> **Toujours repasser les mêmes `-f`** à chaque `helm upgrade` : Helm repart des valeurs par défaut + ce que tu fournis. Oublier `-f values-dev.yaml` remettrait les images GHCR (et donc des pods en `ImagePullBackOff`). `--reuse-values` réutilise les valeurs de la révision précédente, mais masque ce qui est réellement déployé : on préfère des fichiers explicites.

## Choix de conception

| Choix | Pourquoi |
|---|---|
| Labels `app.kubernetes.io/*` dans `_helpers.tpl` | Labels standard de Kubernetes : outils (kubectl, Grafana, Lens) et humains savent à quelle application/release/version appartient chaque objet |
| Labels de **sélection** sans version | Le sélecteur d'un Deployment est immuable : y mettre la version bloquerait tout `upgrade` |
| Annotations `checksum/config` et `checksum/secret` | Les variables d'environnement ne sont lues qu'au démarrage : une ConfigMap modifiée seule ne change rien aux pods. L'empreinte dans le modèle de pod force un rolling update |
| `secrets.existingSecret` | En production, les secrets viennent d'un outil dédié (Sealed Secrets, External Secrets, Vault) : le chart sait alors utiliser un Secret existant au lieu d'en créer un |
| `helm.sh/resource-policy: keep` sur le PVC des images | `helm uninstall` ne doit pas effacer les fichiers des utilisateurs |
| PVC de PostGIS via `volumeClaimTemplates` | Jamais supprimé par Helm (créé par le StatefulSet) : les données survivent à un uninstall. Pour repartir de zéro : `kubectl -n carrefourconnect delete pvc --all` |
| `postgis.enabled` / `externalDatabase.url` | Le même chart peut utiliser une base managée (RDS, Cloud SQL...) en production |
| `helm test` | Vérifie le déploiement depuis l'intérieur du cluster ; réutilisé par la CI (étape 5) |

## Incident rencontré : doublons au démarrage simultané

Au premier `helm install`, les 2 pods backend ont exécuté l'initialisation des données en même temps et créé **deux** rôles ADMIN (« vérifier puis insérer » n'est pas atomique). Le `helm upgrade` suivant a échoué : chaque nouveau pod plantait (`Query did not return a unique result`). `helm rollback` a remis l'application dans l'état de la révision 1, puis le backend a été corrigé (contraintes uniques en base + initialisation qui tolère l'insertion concurrente, voir `DataInitializer`). Après correction, les logs montrent le second pod qui affiche `rôle ADMIN déjà créé(e) par une autre instance, on continue.`
