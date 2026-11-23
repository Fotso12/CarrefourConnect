#!/usr/bin/env bash
# Équivalent Linux / macOS / WSL de cluster-down.ps1.
#   ./deploy/scripts/cluster-down.sh           supprime le cluster
#   ./deploy/scripts/cluster-down.sh --stop    l'arrête seulement (données conservées)
set -euo pipefail

CLUSTER_NAME="${CLUSTER_NAME:-carrefourconnect}"

if ! k3d cluster list "$CLUSTER_NAME" >/dev/null 2>&1; then
  echo "Aucun cluster '$CLUSTER_NAME' : rien à faire."
  exit 0
fi

if [[ "${1:-}" == "--stop" ]]; then
  k3d cluster stop "$CLUSTER_NAME"
else
  k3d cluster delete "$CLUSTER_NAME"
fi
