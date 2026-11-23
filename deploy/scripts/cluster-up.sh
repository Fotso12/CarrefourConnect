#!/usr/bin/env bash
# Équivalent Linux / macOS / WSL de cluster-up.ps1 : crée le cluster k3d local.
#   ./deploy/scripts/cluster-up.sh            (variables optionnelles : CLUSTER_NAME, K3S_IMAGE, AGENTS, HTTP_PORT)
set -euo pipefail

CLUSTER_NAME="${CLUSTER_NAME:-carrefourconnect}"
K3S_IMAGE="${K3S_IMAGE:-rancher/k3s:v1.36.6-k3s1}"   # version figée, alignée sur kubectl 1.36
AGENTS="${AGENTS:-1}"
HTTP_PORT="${HTTP_PORT:-80}"

for tool in docker k3d kubectl; do
  command -v "$tool" >/dev/null || { echo "'$tool' introuvable (voir deploy/SETUP.md)" >&2; exit 1; }
done
docker info >/dev/null 2>&1 || { echo "Le moteur Docker ne répond pas" >&2; exit 1; }

if k3d cluster list "$CLUSTER_NAME" >/dev/null 2>&1; then
  echo "Le cluster '$CLUSTER_NAME' existe déjà : démarrage."
  k3d cluster start "$CLUSTER_NAME" --wait
else
  k3d cluster create "$CLUSTER_NAME" \
    --image "$K3S_IMAGE" \
    --servers 1 \
    --agents "$AGENTS" \
    --port "${HTTP_PORT}:80@loadbalancer" \
    --wait --timeout 180s
fi

kubectl config use-context "k3d-$CLUSTER_NAME" >/dev/null

# k3s installe Traefik et metrics-server juste après le démarrage des nœuds : on attend.
kubectl -n kube-system wait --for=condition=complete job/helm-install-traefik --timeout=180s >/dev/null
kubectl -n kube-system rollout status deployment/traefik --timeout=180s
kubectl -n kube-system rollout status deployment/metrics-server --timeout=180s
kubectl get nodes -o wide
kubectl get pods -n kube-system
