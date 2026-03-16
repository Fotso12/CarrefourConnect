# Étape 1 : Phase de construction (build)
FROM eclipse-temurin:17-jdk-focal AS build
WORKDIR /app

# Copie de l'exécutable Maven et du fichier pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Résolution des dépendances (couche de cache)
RUN ./mvnw dependency:go-offline

# Copie du code source et construction du projet
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Étape 2 : Phase d'exécution (runtime)
FROM eclipse-temurin:17-jre-focal
WORKDIR /app

# Copie de l'artefact construit depuis la phase build
COPY --from=build /app/target/*.jar app.jar

# Exposition du port de l'application
EXPOSE 8084

# Lancement de l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
