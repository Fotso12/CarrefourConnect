# ==========================================
# Build Flutter APK (release)
# ==========================================
FROM ghcr.io/cirruslabs/flutter:stable AS build
WORKDIR /app

# Copie des fichiers de dépendances
COPY pubspec.yaml pubspec.lock ./

# Téléchargement des dépendances
RUN flutter pub get

# Copie du code source complet
COPY . .

# Build de l'APK en mode release
RUN flutter build apk --release

# ==========================================
# Étape d'export : image légère pour recupérer l'APK
# ==========================================
FROM alpine:latest
WORKDIR /output

# Copie de l'APK généré
COPY --from=build /app/build/app/outputs/flutter-apk/app-release.apk ./carrefourconnect.apk

# L'APK est disponible dans /output/carrefourconnect.apk
CMD ["sh", "-c", "echo 'APK disponible : /output/carrefourconnect.apk' && ls -lh /output/"]
