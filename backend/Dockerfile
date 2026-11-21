# syntax=docker/dockerfile:1
# ============================================================
# Image du backend CarrefourConnect (Spring Boot 4, Java 25)
# Construction en plusieurs étapes ("multi-stage") : l'image finale ne contient
# ni Maven, ni le JDK, ni le code source, seulement un JRE et l'application.
# ============================================================

# ---------- Étape 1 : compilation (JDK complet + Maven) ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /build

# On copie d'abord uniquement ce qui décrit les dépendances : tant que pom.xml ne
# change pas, Docker réutilise cette couche et ne retélécharge rien.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
# --mount=type=cache garde le dépôt Maven (~/.m2) entre deux builds (BuildKit).
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q dependency:go-offline

# Puis le code source, qui change souvent.
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q package -DskipTests \
    && cp target/*.jar application.jar

# Découpage du jar en couches (dépendances / chargeur Spring / code applicatif).
# Les dépendances changent rarement : en les plaçant dans une couche séparée,
# une modification du code ne fait retransférer que quelques centaines de Ko.
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# ---------- Étape 2 : exécution (JRE seul, utilisateur non-root) ----------
FROM eclipse-temurin:25-jre

# Utilisateur dédié sans privilèges, avec un UID NUMÉRIQUE fixe (1001) : Kubernetes peut
# ainsi vérifier "runAsNonRoot" sans avoir à lire /etc/passwd dans l'image.
RUN groupadd --system --gid 1001 spring \
    && useradd --system --uid 1001 --gid spring --no-create-home --shell /usr/sbin/nologin spring

WORKDIR /app
# Dossiers où l'application écrit (images envoyées, logs hors profil container) :
# ils doivent appartenir à l'utilisateur non-root, sinon "Permission denied".
RUN mkdir -p /app/uploads /app/logs && chown -R 1001:1001 /app

# Une couche par catégorie, de la moins souvent modifiée à la plus souvent modifiée.
COPY --from=build --chown=1001:1001 /build/extracted/dependencies/ ./
COPY --from=build --chown=1001:1001 /build/extracted/spring-boot-loader/ ./
COPY --from=build --chown=1001:1001 /build/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=1001:1001 /build/extracted/application/ ./

USER 1001:1001

# Options JVM adaptées aux conteneurs, lues automatiquement par la JVM :
# - MaxRAMPercentage=75 : le tas (heap) peut utiliser 75 % de la mémoire ALLOUÉE AU
#   CONTENEUR (limite Docker/Kubernetes), pas de la RAM du PC. Les 25 % restants
#   servent au reste de la JVM (métaspace, threads, buffers).
# - InitialRAMPercentage=50 : on réserve la moitié dès le démarrage (moins de pauses).
# - ExitOnOutOfMemoryError : en cas de manque de mémoire, la JVM s'arrête net ;
#   Kubernetes redémarre alors le pod au lieu de le laisser dans un état instable.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=50 -XX:+ExitOnOutOfMemoryError"
# Profil "container" : logs uniquement sur la sortie standard (voir logback-spring.xml).
ENV SPRING_PROFILES_ACTIVE=container

EXPOSE 8084

# Forme "exec" (tableau JSON) : java est le processus n°1 du conteneur et reçoit
# directement le signal d'arrêt (SIGTERM) envoyé par Docker/Kubernetes.
ENTRYPOINT ["java", "-jar", "application.jar"]
