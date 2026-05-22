# Guide de Déploiement CarrefourConnect (détaillé)

Ce guide détaille étape par étape le déploiement du projet CarrefourConnect : backend Spring Boot/PostGIS, frontend Angular, et mobile Flutter avec Firebase. Il est adapté à la structure réelle de ton projet.

---

## 1. Backend Spring Boot + PostgreSQL/PostGIS sur Render


### 1.1. Préparer le code
- Le code backend se trouve dans le dossier `backend/`.
- Les dépendances nécessaires sont déjà présentes dans le `pom.xml` : Spring Boot, WebSocket, Mail, PostgreSQL, PostGIS, etc.
- Les fichiers sensibles (`application.properties`, credentials) doivent utiliser des **variables d'environnement** (jamais de secrets dans le code !).
- **Vérifie que tu utilises bien les variables d'environnement dans `application.properties`** :
  ```properties
  spring.datasource.url=${SPRING_DATASOURCE_URL}
  spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
  spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
  spring.jpa.hibernate.ddl-auto=update
  spring.mail.username=${MAIL_USERNAME}
  spring.mail.password=${MAIL_PASSWORD}
  spring.mail.host=smtp.sendgrid.net # ou smtp.gmail.com selon ton provider
  spring.mail.port=587
  spring.mail.properties.mail.smtp.auth=true
  spring.mail.properties.mail.smtp.starttls.enable=true
  # ...
  ```

### 1.1.1. (Optionnel) Adapter l'envoi d'emails pour SendGrid
- **Pourquoi ?** Gmail bloque souvent les apps tierces, SendGrid est gratuit jusqu'à 100 mails/jour et facile à intégrer.
- Crée un compte sur https://sendgrid.com/ ("Start For Free").
- Crée une API Key ("Email API > Integration Guide > Web API or SMTP Relay").
- Pour SMTP :
  - Host : `smtp.sendgrid.net`
  - Port : `587`
  - Username : `apikey`
  - Password : ta clé API
- Mets ces infos dans Render (voir plus bas).
- **Dans le code** :
  - Utilise `spring.mail.username=apikey` et `spring.mail.password=<ta clé API>`
  - Pas besoin de changer le code Java si tu utilises `JavaMailSender` de Spring Boot.

### 1.2. Créer un compte Render

### 1.2. Créer un compte Render
- Inscris-toi sur https://render.com/ (GitHub recommandé).

### 1.3. Créer la base PostgreSQL/PostGIS
- Dashboard Render > New > PostgreSQL
- Coche **PostGIS**
- Note les infos de connexion (host, port, user, password, dbname)

**Astuce** : Après création, va dans la base Render > "Connect" pour copier l'URL JDBC complète.

### 1.4. Déployer l'application Spring Boot
- Dashboard Render > New > Web Service
- Source : ton repo GitHub, dossier `backend/`
- **Build Command** :
  ```
  ./mvnw clean package
  ```
- **Start Command** :
  ```
  java -jar target/*.jar
  ```
- **Variables d'environnement** :
  - `SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:<port>/<dbname>`
  - `SPRING_DATASOURCE_USERNAME=<user>`
  - `SPRING_DATASOURCE_PASSWORD=<password>`
  - `MAIL_USERNAME`, `MAIL_PASSWORD`, etc.
- **Port** : 8084 (selon ta config)

#### Exemple de variables d'environnement à ajouter sur Render
```
SPRING_DATASOURCE_URL=jdbc:postgresql://dpg-xxxxxx.render.com:5432/carrefourconnect
SPRING_DATASOURCE_USERNAME=carrefourconnect_user
SPRING_DATASOURCE_PASSWORD=motdepasse
MAIL_USERNAME=apikey
MAIL_PASSWORD=SG.xxxxxxxx
SPRING_MAIL_HOST=smtp.sendgrid.net
SPRING_MAIL_PORT=587
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
JWT_SECRET=unevaleursecrete
ADMIN_EMAIL=ton@email.com
```

#### Pour les fichiers uploadés (images, etc.)
- Render ne garde pas les fichiers uploadés sur le disque !
- Utilise AWS S3, Google Cloud Storage, ou Backblaze B2.
- Pour AWS S3 :
  - Crée un bucket sur https://s3.console.aws.amazon.com/
  - Crée un utilisateur IAM avec accès S3, récupère `AWS_ACCESS_KEY_ID` et `AWS_SECRET_ACCESS_KEY`.
  - Mets ces variables dans Render.
  - Utilise la lib Java AWS S3 SDK dans ton backend pour uploader/lire les fichiers.

### 1.5. Stockage fichiers (uploads)
- Render ne garde pas les fichiers uploadés : utilise AWS S3, Google Cloud Storage, etc.
- Mets les credentials dans Render et adapte le code si besoin.

---

## 2. Frontend Angular

### 2.1. Préparer le build
- Le code Angular est dans `frontend-web/`.
- Modifie `src/environments/environment.prod.ts` pour pointer vers l’URL Render du backend.
- Build local :
  ```
  npm install
  ng build --configuration production
  ```

#### Exemple de `environment.prod.ts`
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://carrefourconnect-backend.onrender.com',
  wsUrl: 'wss://carrefourconnect-backend.onrender.com/ws'
};
```

### 2.2. Déployer sur Render (ou Vercel/Netlify)
- **Render** : Dashboard > New > Static Site
  - Source : repo GitHub, dossier `frontend-web/`
  - **Build Command** :
    ```
    npm install && npm run build -- --configuration production
    ```
  - **Publish Directory** :
    ```
    dist/<nom-du-projet>
    ```
- **Vercel/Netlify** : même principe, build command et publish directory identiques.

#### Pour les variables d'environnement Angular
- Angular ne lit pas les variables d'environnement système à l'exécution, il faut les "hardcoder" dans `environment.prod.ts` ou utiliser un script de remplacement lors du build.

---

## 3. Mobile Flutter + Firebase

### 3.1. Créer un projet Firebase
- https://console.firebase.google.com/
- Ajoute une appli Android/iOS (télécharge `google-services.json` ou `GoogleService-Info.plist`)
- Active Auth, Firestore, Storage, Cloud Messaging

#### Détail étape par étape
1. Va sur https://console.firebase.google.com/ > "Ajouter un projet" > nomme-le (ex: CarrefourConnect)
2. Ajoute une appli Android :
  - Saisis le nom du package (ex: `com.carrefourconnect.mobile`)
  - Télécharge `google-services.json` et place-le dans `frontend-mobile/android/app/`
3. Ajoute une appli iOS :
  - Saisis le bundle ID (ex: `com.carrefourconnect.mobile`)
  - Télécharge `GoogleService-Info.plist` et place-le dans `frontend-mobile/ios/Runner/`
4. Active les services nécessaires dans la console Firebase :
  - Authentification (Email/Password, Google, etc.)
  - Firestore Database
  - Storage (pour images, etc.)
  - Cloud Messaging (pour notifications push)

### 3.2. Configurer Flutter
- Place les fichiers Firebase dans `android/app/` et `ios/Runner/`
- Ajoute dans `pubspec.yaml` :
  ```yaml
  dependencies:
    firebase_core: ^2.0.0
    firebase_auth: ^4.0.0
    cloud_firestore: ^4.0.0
    firebase_messaging: ^14.0.0
    firebase_storage: ^11.0.0
  ```
- Suis la doc officielle : https://firebase.flutter.dev/docs/overview/

#### Initialisation dans le code Dart
Dans `main.dart` :
```dart
void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp();
  runApp(MyApp());
}
```

#### Notifications Push (Firebase Cloud Messaging)
Dans `pubspec.yaml` ajoute :
```yaml
firebase_messaging: ^14.0.0
```
Dans le code :
```dart
FirebaseMessaging messaging = FirebaseMessaging.instance;
NotificationSettings settings = await messaging.requestPermission();
String? token = await messaging.getToken();
print('FCM Token: $token');
```
Pour recevoir les notifications, configure le backend pour envoyer les messages via l’API FCM (https://firebase.google.com/docs/cloud-messaging/send-message).

### 3.3. Déployer l’appli mobile
- Android : génère APK/AAB et publie sur Google Play
- iOS : compile avec Xcode et publie sur App Store
- Web : `flutter build web` puis `firebase deploy`

#### Déploiement web sur Firebase Hosting
1. Installe Firebase CLI :
  ```bash
  npm install -g firebase-tools
  ```
2. Connecte-toi :
  ```bash
  firebase login
  ```
3. Initialise l’hébergement dans `frontend-mobile/` :
  ```bash
  firebase init hosting
  ```
4. Build le projet :
  ```bash
  flutter build web
  ```
5. Déploie :
  ```bash
  firebase deploy
  ```

---

## 4. OpenStreetMap et PostGIS
- OpenStreetMap : pas de compte à créer, tu utilises Leaflet/Nominatim dans le frontend.
- PostGIS : activé sur la base Render.
- Attention : pour beaucoup de requêtes Nominatim, utiliser un provider commercial ou auto-hébergé.

#### Conseils pratiques
- Pour la géolocalisation, tu utilises déjà Leaflet côté Angular (`frontend-web/`).
- Pour le géocodage (adresse → coordonnées), utilise Nominatim ou un service équivalent.
- Si tu dépasses les quotas gratuits de Nominatim, regarde Mapbox, OpenCage, ou monte ton propre serveur Nominatim.

---

## 5. SMTP (envoi d’emails)
- Utilise Gmail, SendGrid, Mailgun, etc.
- Crée un compte, récupère les identifiants SMTP, mets-les dans Render (`MAIL_USERNAME`, `MAIL_PASSWORD`...)

#### Exemple d’intégration SendGrid côté backend
Dans `pom.xml` :
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```
Dans `application.properties` :
```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=SG.xxxxxxxx
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```
Dans le code Java, continue d’utiliser `JavaMailSender`.

---

## 6. Comptes à créer
- Render (https://render.com/)
- GitHub (https://github.com/)
- Firebase (https://console.firebase.google.com/)
- Vercel/Netlify (optionnel)
- Provider SMTP (Gmail, SendGrid...)
- (Optionnel) AWS S3 ou équivalent

#### Récapitulatif des comptes et services
- **Render** : hébergement backend, base de données, frontend statique
- **GitHub** : gestion du code source
- **Firebase** : notifications push, auth mobile, hébergement web mobile
- **SendGrid** (ou autre SMTP) : email transactionnel
- **AWS S3** (ou équivalent) : stockage fichiers uploadés

---

## 7. Sécurité & bonnes pratiques
- Utilise les variables d’environnement pour tous les secrets
- Active CORS sur le backend
- Utilise HTTPS partout
- Désactive le mode debug en prod

#### Conseils supplémentaires
- **Ne commit jamais de secrets dans Git !**
- Sur Render, utilise l’onglet "Environment" pour gérer les variables.
- Pour la sécurité mobile, active la validation des domaines Firebase (App Check).
- Pour Angular, vérifie que les URLs d’API sont bien en HTTPS.

---

## 8. Liens utiles
- [Render Docs](https://render.com/docs)
- [Déployer Angular sur Render](https://render.com/docs/deploy-angular)
- [Déployer Spring Boot sur Render](https://render.com/docs/deploy-spring)
- [Firebase Flutter](https://firebase.flutter.dev/)
- [PostGIS sur Render](https://render.com/docs/databases#postgis)

- [SendGrid SMTP Spring Boot](https://docs.sendgrid.com/for-developers/sending-email/spring-boot)
- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [AWS S3 Java SDK](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-s3.html)

---

**Besoin d’un guide détaillé pour une étape ? Dis-le-moi !**

