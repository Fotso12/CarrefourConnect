# ==========================================
# Étape 1 : Build Angular (Node)
# ==========================================
FROM node:20-alpine AS build
WORKDIR /app

# Copier d'abord les fichiers de dépendances (optimisation cache Docker)
COPY package*.json ./
RUN npm ci --legacy-peer-deps

# Copier le reste du code source et construire
COPY . .
RUN npm run build -- --configuration production

# ==========================================
# Étape 2 : Serveur Web (Nginx)
# ==========================================
FROM nginx:alpine

# Supprimer la configuration Nginx par défaut
RUN rm /etc/nginx/conf.d/default.conf

# Configuration Nginx pour une SPA Angular (HTML5 routing)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copier l'artefact Angular compilé depuis l'étape de build
COPY --from=build /app/dist/carrefourconnect-web/browser /usr/share/nginx/html

# Exposer le port HTTP
EXPOSE 80

# Démarrer Nginx en mode foreground
CMD ["nginx", "-g", "daemon off;"]
