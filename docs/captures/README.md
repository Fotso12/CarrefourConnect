# Captures d'écran du README

Le README principal affiche les 4 images ci-dessous (section « Déploiement Kubernetes → Captures »). Enregistre-les dans ce dossier **avec exactement ces noms**, en PNG, idéalement en 1600 px de large maximum (`Win + Maj + S` pour capturer une zone sous Windows).

| Fichier | Quoi capturer | Comment l'obtenir |
|---|---|---|
| `01-pods.png` | Le terminal avec les pods de l'application, tous `Running` / `1/1`, et la colonne NODE | `kubectl -n carrefourconnect get pods -o wide` (ajouter `kubectl -n carrefourconnect get svc,ingress,hpa` en dessous si la place le permet) |
| `02-pipeline.png` | Le graphe des jobs d'une exécution **verte** sur `main` (ou d'un tag de release) | GitHub → onglet **Actions** → workflow CI/CD → cliquer sur la dernière exécution verte |
| `03-grafana-charge.png` | Le dashboard **CarrefourConnect - Backend** pendant le test de charge : débit en hausse, latence p95, CPU qui dépasse la ligne rouge | Lancer le test k6 (voir `deploy/monitoring/README.md`), attendre ~3 min, période « Last 15 minutes » |
| `04-hpa.png` | Le HPA qui ajoute des pods : soit le panneau Grafana « Réplicas backend (HPA) » qui monte de 2 à 4, soit le terminal | `kubectl -n carrefourconnect get hpa -w` pendant le test (TARGETS > 70 %, REPLICAS 2 → 4), à côté de `kubectl -n carrefourconnect get pods -w` |

Conseils :

- Masquer les informations personnelles éventuelles (onglets, notifications).
- Pour Grafana, le thème sombre et le mode plein écran (touche `d` puis `k`, ou l'icône « kiosk ») rendent mieux.
- Une 5e capture optionnelle intéressante pour un portfolio : la page de la **release v0.2.0** sur GitHub (notes + chart Helm en pièce jointe).
