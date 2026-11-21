<#
.SYNOPSIS
    Crée le cluster Kubernetes local CarrefourConnect avec k3d (k3s dans Docker).

.DESCRIPTION
    - 1 nœud "server" (plan de contrôle : API Kubernetes, ordonnanceur...) + N nœuds "agent"
      (nœuds de travail où tournent les pods). Chaque nœud est un conteneur Docker.
    - Le port 80 de ta machine est relié au load balancer de k3d, qui transmet à Traefik,
      l'Ingress Controller fourni par k3s : http://localhost arrivera sur l'Ingress.
    - La version de k3s est FIGÉE : le cluster sera identique à chaque recréation.

.EXAMPLE
    .\deploy\scripts\cluster-up.ps1
    .\deploy\scripts\cluster-up.ps1 -Agents 0      # un seul nœud, pour économiser la RAM
#>
param(
    [string] $ClusterName = "carrefourconnect",
    # k3s 1.36 : même version mineure que kubectl (Kubernetes accepte un écart de ±1 entre les deux)
    [string] $K3sImage = "rancher/k3s:v1.36.6-k3s1",
    [ValidateRange(0, 3)]
    [int] $Agents = 1,
    [int] $HttpPort = 80
)

$ErrorActionPreference = "Stop"

function Test-Command($name) { return [bool](Get-Command $name -ErrorAction SilentlyContinue) }

# --- 1. Prérequis ---------------------------------------------------------------
foreach ($tool in "docker", "k3d", "kubectl") {
    if (-not (Test-Command $tool)) { throw "'$tool' est introuvable dans le PATH. Voir deploy/SETUP.md (étape 2)." }
}
docker info --format "{{.ServerVersion}}" *> $null
if ($LASTEXITCODE -ne 0) { throw "Le moteur Docker ne répond pas : démarre Docker Desktop et attends 'Engine running'." }

# --- 2. Le cluster existe-t-il déjà ? --------------------------------------------
$existing = k3d cluster list --output json | ConvertFrom-Json | Where-Object { $_.name -eq $ClusterName }
if ($existing) {
    Write-Host "Le cluster '$ClusterName' existe déjà : on s'assure qu'il est démarré." -ForegroundColor Yellow
    k3d cluster start $ClusterName --wait
} else {
    # Le port doit être libre (Docker Compose utilise 8080, donc pas de conflit).
    if (Get-NetTCPConnection -LocalPort $HttpPort -State Listen -ErrorAction SilentlyContinue) {
        throw "Le port $HttpPort est déjà utilisé. Libère-le ou relance avec -HttpPort 8081."
    }

    Write-Host "Création du cluster '$ClusterName' ($K3sImage, 1 server + $Agents agent(s))..." -ForegroundColor Cyan
    k3d cluster create $ClusterName `
        --image $K3sImage `
        --servers 1 `
        --agents $Agents `
        --port "${HttpPort}:80@loadbalancer" `
        --wait --timeout 180s
    if ($LASTEXITCODE -ne 0) { throw "La création du cluster a échoué (voir les messages ci-dessus)." }
}

# --- 3. kubectl pointe sur ce cluster ---------------------------------------------
# k3d ajoute le cluster dans ~/.kube/config sous le nom de contexte "k3d-<nom>".
kubectl config use-context "k3d-$ClusterName" | Out-Null

# --- 4. Attendre les composants système -------------------------------------------
# Les nœuds sont "Ready" en ~15 s, mais k3s installe Traefik (via un Job Helm) et
# metrics-server juste après : sans cette attente, http://localhost ne répondrait pas encore.
Write-Host "Attente de Traefik (Ingress) et de metrics-server..." -ForegroundColor Cyan
kubectl -n kube-system wait --for=condition=complete job/helm-install-traefik --timeout=180s | Out-Null
kubectl -n kube-system rollout status deployment/traefik --timeout=180s
kubectl -n kube-system rollout status deployment/metrics-server --timeout=180s

Write-Host "`nNœuds du cluster :" -ForegroundColor Green
kubectl get nodes -o wide

Write-Host "`nComposants système (Traefik, CoreDNS, metrics-server...) :" -ForegroundColor Green
kubectl get pods -n kube-system

Write-Host "`nCluster prêt. L'Ingress répondra sur http://localhost$(if ($HttpPort -ne 80) { ":$HttpPort" })" -ForegroundColor Green
