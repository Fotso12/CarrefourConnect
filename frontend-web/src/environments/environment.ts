// Configuration de DÉVELOPPEMENT (ng serve sur http://localhost:4200).
// Le frontend et le backend tournent sur deux ports différents : on appelle donc
// le backend par son URL complète.
// En production, ce fichier est remplacé par environment.prod.ts (voir
// "fileReplacements" dans angular.json).
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8084',
  wsUrl: 'http://localhost:8084'
};
