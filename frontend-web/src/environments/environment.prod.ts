// Configuration de PRODUCTION (ng build, image Docker, Kubernetes).
// URL vides = chemins RELATIFS : le navigateur appelle /api/... et /ws sur le MÊME
// domaine que celui qui a servi la page (http://localhost avec Docker Compose ou k3d,
// ou n'importe quel nom de domaine plus tard).
// C'est le reverse proxy devant l'application (Nginx en local, l'Ingress dans
// Kubernetes) qui redirige /api, /ws et /uploads vers le backend.
// Avantages : une seule image valable partout, et plus besoin de CORS
// puisque tout vient de la même origine.
export const environment = {
  production: true,
  apiUrl: '',
  wsUrl: ''
};
