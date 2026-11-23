// ============================================================
// Test de charge k6 : simule des visiteurs qui consultent l'annuaire.
// Uniquement des endpoints PUBLICS en lecture (aucune donnée créée ni modifiée).
//
// Lancement sans rien installer (k6 dans un conteneur Docker), depuis la racine du dépôt :
//   Get-Content deploy\load-test\charge.js | docker run --rm -i -e BASE_URL=http://host.docker.internal grafana/k6:2.4.0 run -
// ("host.docker.internal" = la machine Windows vue depuis un conteneur ; l'Ingress k3d y écoute sur le port 80)
//
// Variables : BASE_URL (défaut http://localhost), DUREE_PALIER (défaut 2m), VU_MAX (défaut 60)
// ============================================================
import http from 'k6/http';
import { check, group, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost';
const PALIER = __ENV.DUREE_PALIER || '2m';
const VU_MAX = parseInt(__ENV.VU_MAX || '60', 10);

// Un "VU" (virtual user) est un visiteur simulé qui enchaîne les pages en boucle.
// Profil en paliers : montée progressive, plateau, pic, puis redescente. Le pic doit
// pousser le CPU des pods au-dessus de la cible du HPA (175m) pour déclencher l'autoscaling.
export const options = {
  scenarios: {
    visiteurs: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: Math.round(VU_MAX / 4) },   // échauffement
        { duration: PALIER, target: Math.round(VU_MAX / 2) },  // trafic soutenu
        { duration: PALIER, target: VU_MAX },                  // pic : le HPA doit réagir
        { duration: '1m', target: VU_MAX },                    // maintien du pic
        { duration: '30s', target: 0 },                        // fin du trafic
      ],
      gracefulRampDown: '10s',
    },
  },
  // Seuils : le test est "en échec" (code de sortie non nul) s'ils ne sont pas respectés
  thresholds: {
    http_req_failed: ['rate<0.02'],          // moins de 2 % d'erreurs
    http_req_duration: ['p(95)<1500'],       // 95 % des requêtes en moins de 1,5 s
  },
};

// Quelques points de Douala et de Yaoundé pour varier les recherches de proximité
const POSITIONS = [
  { lat: 4.0511, lon: 9.7679 },   // Douala - Akwa
  { lat: 4.0614, lon: 9.7861 },   // Douala - Bonapriso
  { lat: 4.0083, lon: 9.7380 },   // Douala - Bonabéri
  { lat: 3.8480, lon: 11.5021 },  // Yaoundé - centre
];
const RAYONS_KM = [2, 5, 10, 20];

function auHasard(tableau) {
  return tableau[Math.floor(Math.random() * tableau.length)];
}

// Ce que fait chaque visiteur simulé, en boucle
export default function () {
  const pos = auHasard(POSITIONS);

  group('page d\'accueil : catégories', () => {
    const res = http.get(`${BASE_URL}/api/categories`, { tags: { endpoint: 'categories' } });
    check(res, { 'catégories : 200': (r) => r.status === 200 });
  });

  group('recherche autour de moi', () => {
    const rayon = auHasard(RAYONS_KM);
    const res = http.get(
      `${BASE_URL}/api/commerces/rechercher?lat=${pos.lat}&lon=${pos.lon}&rayon=${rayon}`,
      { tags: { endpoint: 'rechercher' } },
    );
    check(res, { 'recherche : 200': (r) => r.status === 200 });
  });

  group('carte : commerces à proximité', () => {
    const distanceM = auHasard(RAYONS_KM) * 1000;
    const res = http.get(
      `${BASE_URL}/api/commerces/proximite?lat=${pos.lat}&lon=${pos.lon}&distance=${distanceM}`,
      { tags: { endpoint: 'proximite' } },
    );
    check(res, { 'proximité : 200': (r) => r.status === 200 });
  });

  group('liste des commerces', () => {
    const res = http.get(`${BASE_URL}/api/commerces`, { tags: { endpoint: 'commerces' } });
    check(res, { 'liste : 200': (r) => r.status === 200 });
  });

  // Temps de lecture d'un vrai visiteur entre deux pages (0,5 à 1,5 s)
  sleep(0.5 + Math.random());
}
