<#
.SYNOPSIS
    Supprime le cluster k3d CarrefourConnect (ou l'arrête seulement avec -StopOnly).

.DESCRIPTION
    La suppression efface les conteneurs des nœuds ET tout ce qui tournait dedans
    (pods, volumes de la base...). Les images Docker de ta machine sont conservées.
    -StopOnly arrête les nœuds sans rien effacer : le cluster redémarre tel quel avec
    cluster-up.ps1 (pratique pour libérer la RAM le soir).

.EXAMPLE
    .\deploy\scripts\cluster-down.ps1
    .\deploy\scripts\cluster-down.ps1 -StopOnly
#>
param(
    [string] $ClusterName = "carrefourconnect",
    [switch] $StopOnly
)

$ErrorActionPreference = "Stop"

$existing = k3d cluster list --output json | ConvertFrom-Json | Where-Object { $_.name -eq $ClusterName }
if (-not $existing) {
    Write-Host "Aucun cluster '$ClusterName' : rien à faire." -ForegroundColor Yellow
    return
}

if ($StopOnly) {
    k3d cluster stop $ClusterName
    Write-Host "Cluster '$ClusterName' arrêté (données conservées). Relance : .\deploy\scripts\cluster-up.ps1" -ForegroundColor Green
} else {
    k3d cluster delete $ClusterName
    Write-Host "Cluster '$ClusterName' supprimé." -ForegroundColor Green
}
