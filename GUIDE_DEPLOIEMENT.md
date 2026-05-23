# Guide de Déploiement CarrefourConnect (Railway, Vercel, Cloudinary, Mailjet, OpenStreetMap)


# 1. Backend Spring Boot + PostgreSQL/PostGIS (Railway)

## 1.1. Créer et configurer le projet Railway
- Connecte-toi sur https://railway.app/
- Clique sur "New Project" > "Provision PostgreSQL" (PostGIS est inclus)
- Note les variables PGHOST, PGPORT, PGDATABASE, PGUSER, PGPASSWORD
- Clique sur "New" > "Deploy from GitHub repo" > choisis ton dossier `backend`
- Dans "Variables", ajoute :
  - `SPRING_DATASOURCE_URL=jdbc:postgresql://<PGHOST>:<PGPORT>/<PGDATABASE>`
  - `SPRING_DATASOURCE_USERNAME=<PGUSER>`
  - `SPRING_DATASOURCE_PASSWORD=<PGPASSWORD>`
  - `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` (voir section 1.3)
  - les variables Mailjet (voir section 1.2)
- Build Command : `./mvnw clean package`
- Start Command : `java -jar target/*.jar`
- Ajoute aussi `PORT=8080` et dans `application.properties` : `server.port=${PORT:8080}`


## 1.2. Envoi d’e-mails avec Mailjet (API Key + API Secret)

### 1.2.1 Récupérer les identifiants Mailjet
- Va sur Mailjet (dashboard)
- Récupère :
  - **Mailjet API Key**
  - **Mailjet API Secret**
- (Optionnel mais recommandé) Choisis :
  - **FROM_EMAIL** : l’email expéditeur (doit être autorisé dans Mailjet)
  - **FROM_NAME** : le nom affiché

### 1.2.2 Où mettre les clés sur Railway
Dans **ton projet Railway (backend)** > **Environment** > **Add variables**, ajoute :
- `MAILJET_API_KEY=<ta_mailjet_api_key>`
- `MAILJET_API_SECRET=<ta_mailjet_api_secret>`
- `MAILJET_FROM_EMAIL=<ton_from_email>`
- `MAILJET_FROM_NAME=<ton_from_name>`

### 1.2.3 Ce que tu dois changer dans le code (backend)
Ton backend utilise déjà `spring.mail.*` via `JavaMailSender` (voir `EmailService.java`).

#### A) Remplacer la configuration SMTP Gmail par Mailjet
Dans `backend/src/main/resources/application.properties`, remplace le bloc Gmail par un bloc générique Mailjet SMTP.

👉 Dans ton fichier, fais en sorte d’avoir (noms d’events identiques à tes variables env) :
```properties
spring.mail.host=${MAILJET_SMTP_HOST:smtp.mailjet.com}
spring.mail.port=${MAILJET_SMTP_PORT:587}
spring.mail.username=${MAILJET_API_KEY}
spring.mail.password=${MAILJET_API_SECRET}

spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

#### B) Vérifier le FROM (expéditeur)
Dans ta classe `EmailService.java`, tu as déjà un `fromEmail` via :
- `@Value("${spring.mail.username:no-reply@carrefourconnect.com}")`

Donc pour que l’expéditeur soit propre :
- soit tu adaptes le `@Value` (recommandé) pour utiliser `MAILJET_FROM_EMAIL`
- soit tu laisses comme actuellement et tu mets `MAILJET_FROM_EMAIL` dans la variable `spring.mail.username` (mais ça peut confondre username Mailjet vs from email)

**Recommandation (propre)** :
- Mettre `fromEmail` sur `MAILJET_FROM_EMAIL` (je te donne la modif exacte si tu veux qu’on fasse aussi la partie code).

### 1.2.4 Checklist de validation
- Les 4 variables Railway existent bien (`MAILJET_API_KEY`, `MAILJET_API_SECRET`, `MAILJET_FROM_EMAIL`, `MAILJET_FROM_NAME`).
- Le `application.properties` pointe bien sur `MAILJET_*` et n’a plus `smtp.gmail.com`.
- Le backend redémarre après ajout des variables.


## 1.3. Stockage d’images avec Cloudinary
- Va sur https://cloudinary.com/ et connecte-toi
- Récupère `cloud_name`, `api_key`, `api_secret` dans le dashboard
- Ajoute ces variables dans Railway
- Dans le backend, ajoute la dépendance Cloudinary Java SDK dans `pom.xml` :
  ```xml
  <dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-http44</artifactId>
    <version>1.37.0</version>
  </dependency>
  ```
- Dans ton service d’upload, utilise Cloudinary pour stocker et servir les images (voir doc officielle)

# 2. Frontend Angular (Vercel)

## 2.1. Préparer le build
- Dans `frontend-web/`, adapte `src/environments/environment.prod.ts` :
  ```typescript
  export const environment = {
    production: true,
    apiUrl: 'https://<ton-backend>.up.railway.app',
    wsUrl: 'wss://<ton-backend>.up.railway.app/ws'
  };
  ```
- Build local :
  ```
  npm install
  ng build --configuration production
  ```

## 2.2. Déployer sur Vercel
- Connecte-toi sur https://vercel.com/
- Clique sur "New Project" > importe ton repo, choisis le dossier `frontend-web`
- Build Command : `npm install && npm run build -- --configuration production`
- Output Directory : `dist/<nom-du-projet>`
- Ajoute un fichier `vercel.json` à la racine de `frontend-web/` :
  ```json
  {
    "rewrites": [
      { "source": "/(.*)", "destination": "/index.html" }
    ]
  }
  ```
- Clique sur "Deploy". L’URL publique sera affichée (ex: https://carrefourconnect-frontend.vercel.app)

# 3. Mobile Flutter (Firebase + APK partagé)

## 3.1. Configurer Firebase
- Va sur https://console.firebase.google.com/
- Crée un projet (ex: CarrefourConnect)
- Ajoute une appli Android :
  - Saisis le nom du package (ex: `com.carrefourconnect.mobile`)
  - Télécharge `google-services.json` et place-le dans `frontend-mobile/android/app/`
- Active :
  - Authentification (Email/Password, Google, etc.)
  - Firestore Database
  - Storage (pour images, etc.)

## 3.2. Intégrer Firebase dans Flutter
- Dans `pubspec.yaml` :
  ```yaml
  dependencies:
    firebase_core: ^2.0.0
    firebase_auth: ^4.0.0
    cloud_firestore: ^4.0.0
    firebase_storage: ^11.0.0
    geolocator: ^10.0.0
  ```
- Dans `main.dart` :
  ```dart
  void main() async {
    WidgetsFlutterBinding.ensureInitialized();
    await Firebase.initializeApp();
    runApp(MyApp());
  }
  ```

## 3.3. Déploiement et partage APK
- Compile un APK :
  ```bash
  flutter build apk --release
  ```
- Récupère le fichier dans `build/app/outputs/flutter-apk/app-release.apk`
- Partage-le via Google Drive, Dropbox, WeTransfer, etc.
- Ajoute un QR code dans ton rapport pour téléchargement rapide

## 3.4. Déploiement web (optionnel)
- `flutter build web`
- Héberge le dossier `build/web` sur Firebase Hosting (voir doc Firebase)

# 4. Géolocalisation gratuite (OpenStreetMap)

## 4.1. Frontend Angular
- Utilise l’API navigateur HTML5 (`navigator.geolocation.getCurrentPosition`) pour obtenir la position
- Pour afficher la carte, utilise Leaflet avec les tuiles OpenStreetMap (pas besoin de clé)
- Pour géocoder une adresse, utilise l’API Nominatim (https://nominatim.openstreetmap.org/)

## 4.2. Flutter
- Utilise le package `geolocator` pour obtenir la position GPS
- Pour afficher une carte, utilise `flutter_map` avec OpenStreetMap
- Pour géocoder une adresse, utilise l’API Nominatim (requêtes HTTP)

# 5. Résumé des services et étapes

- Backend + BDD : Railway (Java + PostgreSQL/PostGIS)
- Stockage images : Cloudinary
- Envoi d’e-mails : Mailjet (API Key + API Secret)
- Frontend Angular : Vercel
- Mobile Flutter : APK partagé, Firebase pour auth/storage/NoSQL, Firebase Hosting pour web
- Géolocalisation : API navigateur, geolocator, Nominatim, OpenStreetMap

# 6. Liens utiles

- [Railway](https://railway.app/)
- [Vercel](https://vercel.com/)
- [Firebase](https://firebase.google.com/)
- [Cloudinary](https://cloudinary.com/)
- [Nominatim](https://nominatim.org/)
- [OpenStreetMap](https://www.openstreetmap.org/)
- [Leaflet](https://leafletjs.com/)
- [flutter_map](https://pub.dev/packages/flutter_map)
- [geolocator](https://pub.dev/packages/geolocator)

---

**Besoin d’un exemple de code ou d’une étape détaillée ? Dis-le-moi !**

---

## 1. Backend Spring Boot + PostgreSQL/PostGIS sur Render


### 1.1. Préparer le code
- Le code backend se trouve dans le dossier `backend/`.
- Les dépendances nécessaires sont déjà présentes dans le `pom.xml` : Spring Boot, WebSocket, Mail, PostgreSQL, PostGIS, etc.
- Les fichiers sensibles (`application.properties`, credentials) doivent utiliser des **variables d'environnement** (jamais de secrets dans le code !).
+- **Vérifie que tu utilises bien les variables d'environnement dans `application.properties`** :
  ```properties
  spring.datasource.url=${SPRING_DATASOURCE_URL}
  spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
  spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
  spring.jpa.hibernate.ddl-auto=update
  spring.mail.username=${MAIL_USERNAME}
  spring.mail.password=${MAIL_PASSWORD}
  spring.mail.host=${MAIL_HOST}
  spring.mail.port=${MAIL_PORT}
  spring.mail.properties.mail.smtp.auth=${MAIL_SMTP_AUTH}
  spring.mail.properties.mail.smtp.starttls.enable=${MAIL_SMTP_STARTTLS}
  cloudinary.cloud_name=${CLOUDINARY_CLOUD_NAME}
  cloudinary.api_key=${CLOUDINARY_API_KEY}
  cloudinary.api_secret=${CLOUDINARY_API_SECRET}
  # ...
  ```

#### Exemple de variables d'environnement à définir sur Render (backend)
```
SPRING_DATASOURCE_URL=jdbc:postgresql://dpg-xxxxxx.render.com:5432/carrefourconnect
SPRING_DATASOURCE_USERNAME=carrefourconnect_user
SPRING_DATASOURCE_PASSWORD=motdepasse
MAIL_USERNAME=apikey
MAIL_PASSWORD=SG.xxxxxxxx
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
JWT_SECRET=unevaleursecrete
ADMIN_EMAIL=ton@email.com
CLOUDINARY_CLOUD_NAME=xxxxxx
CLOUDINARY_API_KEY=xxxxxx
CLOUDINARY_API_SECRET=xxxxxx
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

### 1.5. Stockage fichiers (uploads) avec Cloudinary (gratuit)
- Render ne garde pas les fichiers uploadés : utilise Cloudinary (https://cloudinary.com/), qui a un plan gratuit généreux.
- Crée un compte Cloudinary, récupère `cloud_name`, `api_key`, `api_secret`.
- Mets ces variables dans Render (voir plus haut).
- Utilise la librairie officielle Cloudinary Java :
  - [Cloudinary Java SDK](https://cloudinary.com/documentation/java_integration)
  - Ajoute dans `pom.xml` :
    ```xml
    <dependency>
      <groupId>com.cloudinary</groupId>
      <artifactId>cloudinary-http44</artifactId>
      <version>1.37.0</version>
    </dependency>
    ```
- Dans ton service d’upload, utilise Cloudinary pour stocker et servir les images (voir doc officielle).

---

## 2. Frontend Angular (Render, Redirects/Rewrites)

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


### 2.2. Déployer sur Render avec gestion des routes Angular (éviter 404 au rafraîchissement)
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
  - **Redirects/Rewrites** : Dans Render, va dans l’onglet "Redirects/Rewrites" de ton site statique et ajoute :
    ```
    Source: /*
    Destination: /index.html
    Status: 200
    ```
    Cela permet à Angular de gérer toutes les routes côté client (pas d’erreur 404 au rafraîchissement).

#### Pour les variables d'environnement Angular
- Angular ne lit pas les variables d'environnement système à l'exécution, il faut les "hardcoder" dans `environment.prod.ts` ou utiliser un script de remplacement lors du build.

---

## 3. Mobile Flutter + Firebase (100% gratuit, sans notifications push)


### 3.1. Créer un projet Firebase (gratuit)
- Va sur https://console.firebase.google.com/
- Crée un projet (ex: CarrefourConnect)
- Ajoute une appli Android :
  - Saisis le nom du package (ex: `com.carrefourconnect.mobile`)
  - Télécharge `google-services.json` et place-le dans `frontend-mobile/android/app/`
- Ajoute une appli iOS (optionnel) :
  - Saisis le bundle ID (ex: `com.carrefourconnect.mobile`)
  - Télécharge `GoogleService-Info.plist` et place-le dans `frontend-mobile/ios/Runner/`
- Active uniquement :
  - Authentification (Email/Password, Google, etc.)
  - Firestore Database (NoSQL)
  - Storage (pour images, etc.)
  - **PAS Cloud Messaging** (pas de notifications push pour cette version)

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
    firebase_storage: ^11.0.0
  ```
- Suis la doc officielle : https://firebase.flutter.dev/docs/overview/

#### Initialisation dans le code Dart
Dans `main.dart` :
```dart
void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp();
  runApp(MyApp());
}
```

#### Authentification et base de données
- Utilise `firebase_auth` pour la connexion/inscription.
- Utilise `cloud_firestore` pour stocker les données utilisateurs, commerces, etc.
- Utilise `firebase_storage` pour stocker les images (avatars, photos commerces, etc.).

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


### 3.3. Déployer et partager l’appli mobile (100% gratuit, sans Play Store)

#### Android : Générer un APK ou AAB
1. Dans le dossier `frontend-mobile/` :
  ```bash
  flutter build apk --release
  # ou pour un bundle universel (AAB)
  flutter build appbundle --release
  ```
2. Le fichier APK se trouve dans `build/app/outputs/flutter-apk/app-release.apk`.
3. Le fichier AAB se trouve dans `build/app/outputs/bundle/release/app-release.aab`.

#### Partager l’APK/AAB au jury
- Héberge le fichier APK sur Google Drive, Dropbox, ou un service gratuit (ex: https://wetransfer.com/).
- Envoie le lien de téléchargement au jury (ils peuvent installer l’APK sur un Android sans passer par le Play Store).
- **Astuce** : Ajoute un QR code dans ton rapport ou ta soutenance pour télécharger l’APK facilement.

#### iOS (optionnel)
- Pour iOS, il faut un Mac pour compiler et un compte Apple pour distribuer. Pour une démo gratuite, privilégie Android.

#### Web (optionnel)
1. Build web :
  ```bash
  flutter build web
  ```
2. Héberge le dossier `build/web` sur Firebase Hosting (voir section précédente) ou sur un service gratuit comme Netlify.

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

## 4. OpenStreetMap et PostGIS (géolocalisation gratuite)
- OpenStreetMap : pas de compte à créer, tu utilises Leaflet/Nominatim dans le frontend.
- PostGIS : activé sur la base Render.

- Pour la géolocalisation gratuite :
  - Utilise l’API de géolocalisation native du navigateur (HTML5) ou de Flutter (`geolocator`)
  - Pour le géocodage (adresse → coordonnées), continue d’utiliser Nominatim (gratuit tant que tu restes dans les limites d’usage raisonnable)
  - Si tu veux un reverse-geocoding gratuit, regarde aussi [LocationIQ](https://locationiq.com/) (plan gratuit)

#### Conseils pratiques
- Pour la géolocalisation, tu utilises déjà Leaflet côté Angular (`frontend-web/`).
- Pour le géocodage (adresse → coordonnées), utilise Nominatim ou un service équivalent.
- Si tu dépasses les quotas gratuits de Nominatim, regarde Mapbox, OpenCage, ou monte ton propre serveur Nominatim.

---

## 5. SMTP (envoi d’emails transactionnels)
- Utilise Gmail, SendGrid, Mailgun, etc.
- Crée un compte, récupère les identifiants SMTP, mets-les dans Render (`MAIL_USERNAME`, `MAIL_PASSWORD`...)

#### Exemple d’intégration SendGrid ou Cloudinary côté backend
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

## 6. Comptes à créer et services gratuits
- Railway (https://railway.app/) : backend + base PostgreSQL/PostGIS
- Vercel (https://vercel.com/) : frontend Angular
- Firebase (https://console.firebase.google.com/) : mobile Flutter, hébergement web mobile
- GitHub (https://github.com/) : gestion du code source
- SendGrid (https://sendgrid.com/) : email transactionnel
- Cloudinary (https://cloudinary.com/) : stockage images gratuit

#### Récapitulatif des comptes et services
- **Railway** : hébergement backend, base PostgreSQL/PostGIS
- **Vercel** : hébergement frontend Angular
- **Firebase** : mobile Flutter, hébergement web mobile
- **GitHub** : gestion du code source
- **SendGrid** : email transactionnel
- **Cloudinary** : stockage images gratuit

---

## 7. Sécurité & bonnes pratiques (variables d'environnement, CORS, HTTPS)
 - Utilise les variables d’environnement pour tous les secrets (voir exemples ci-dessus)
- Active CORS sur le backend
- Utilise HTTPS partout
- Désactive le mode debug en prod

#### Conseils supplémentaires
- **Ne commit jamais de secrets dans Git !**
- Sur Render, utilise l’onglet "Environment" pour gérer les variables.
- Pour la sécurité mobile, active la validation des domaines Firebase (App Check).
- Pour Angular, vérifie que les URLs d’API sont bien en HTTPS.

---

- [Railway Docs](https://docs.railway.app/)
- [Déployer Spring Boot sur Railway](https://docs.railway.app/deploy/deploying-spring-boot)
- [Vercel Docs](https://vercel.com/docs)
- [Déployer Angular sur Vercel](https://vercel.com/guides/deploying-angular-with-vercel)
- [Firebase Flutter](https://firebase.flutter.dev/)
- [PostGIS sur Render](https://render.com/docs/databases#postgis)

- [Cloudinary Java SDK](https://cloudinary.com/documentation/java_integration)
- [Cloudinary pour Angular](https://cloudinary.com/documentation/angular_integration)
- [Générer un QR code pour APK](https://www.qr-code-generator.com/)

- [SendGrid SMTP Spring Boot](https://docs.sendgrid.com/for-developers/sending-email/spring-boot)
- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [AWS S3 Java SDK](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-s3.html)

---

**Besoin d’un guide détaillé pour une étape ? Dis-le-moi !**


