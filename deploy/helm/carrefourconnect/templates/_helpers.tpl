{{/*
============================================================
Fonctions réutilisables ("named templates") du chart.
Un fichier commençant par "_" ne produit aucune ressource : il ne contient que des
définitions, appelées depuis les autres templates avec :
  {{ include "carrefourconnect.fullname" . }}
Le "." passé en argument est le contexte courant (.Values, .Release, .Chart...).
============================================================
*/}}

{{/*
Nom du chart, tronqué à 63 caractères : limite des noms DNS dans Kubernetes.
*/}}
{{- define "carrefourconnect.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Nom complet, préfixe de toutes les ressources. Si la release s'appelle déjà
"carrefourconnect", on évite le doublon "carrefourconnect-carrefourconnect".
Deux releases différentes (ex. "demo" et "prod") ont ainsi des noms distincts
et peuvent cohabiter dans le même namespace.
*/}}
{{- define "carrefourconnect.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Nom d'un composant : <fullname>-backend, <fullname>-frontend, <fullname>-postgis.
Usage : {{ include "carrefourconnect.componentName" (dict "root" . "component" "backend") }}
*/}}
{{- define "carrefourconnect.componentName" -}}
{{- printf "%s-%s" (include "carrefourconnect.fullname" .root) .component | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Libellé "chart-version" (ex. carrefourconnect-0.1.0), pour le label helm.sh/chart.
*/}}
{{- define "carrefourconnect.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Labels STANDARDS (recommandés par Kubernetes) posés sur toutes les ressources.
Ils permettent aux outils (kubectl, Lens, Grafana...) de savoir à quelle application,
quelle release et quelle version appartient chaque objet, et qui le gère (Helm).
*/}}
{{- define "carrefourconnect.labels" -}}
helm.sh/chart: {{ include "carrefourconnect.chart" . }}
app.kubernetes.io/name: {{ include "carrefourconnect.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/part-of: carrefourconnect
{{- end }}

{{/*
Labels d'un composant = labels standards + app.kubernetes.io/component.
*/}}
{{- define "carrefourconnect.componentLabels" -}}
{{ include "carrefourconnect.labels" .root }}
app.kubernetes.io/component: {{ .component }}
{{- end }}

{{/*
Labels de SÉLECTION : le lien entre un Deployment/Service et ses pods.
Ils ne doivent JAMAIS changer après l'installation (le sélecteur d'un Deployment
est immuable) : c'est pourquoi on n'y met ni la version ni le nom du chart.
*/}}
{{- define "carrefourconnect.selectorLabels" -}}
app.kubernetes.io/name: {{ include "carrefourconnect.name" .root }}
app.kubernetes.io/instance: {{ .root.Release.Name }}
app.kubernetes.io/component: {{ .component }}
{{- end }}

{{/*
Image complète "dépôt:tag" ; le tag vide prend l'appVersion du chart.
Usage : {{ include "carrefourconnect.image" (dict "image" .Values.backend.image "root" .) }}
*/}}
{{- define "carrefourconnect.image" -}}
{{- printf "%s:%s" .image.repository (default .root.Chart.AppVersion .image.tag) }}
{{- end }}

{{/*
Nom du Secret utilisé : celui fourni (existingSecret) ou celui créé par le chart.
*/}}
{{- define "carrefourconnect.secretName" -}}
{{- if .Values.secrets.existingSecret }}
{{- .Values.secrets.existingSecret }}
{{- else }}
{{- include "carrefourconnect.fullname" . }}-secrets
{{- end }}
{{- end }}

{{/*
Nom de la ConfigMap.
*/}}
{{- define "carrefourconnect.configMapName" -}}
{{- include "carrefourconnect.fullname" . }}-config
{{- end }}

{{/*
Hôte de la base : le Service headless de PostGIS du chart.
*/}}
{{- define "carrefourconnect.postgisHost" -}}
{{- include "carrefourconnect.componentName" (dict "root" . "component" "postgis") }}
{{- end }}

{{/*
URL JDBC et utilisateur de la base : PostGIS du chart, ou base externe.
*/}}
{{- define "carrefourconnect.datasourceUrl" -}}
{{- if .Values.postgis.enabled }}
{{- printf "jdbc:postgresql://%s:5432/%s" (include "carrefourconnect.postgisHost" .) .Values.postgis.database }}
{{- else }}
{{- required "externalDatabase.url est obligatoire quand postgis.enabled=false" .Values.externalDatabase.url }}
{{- end }}
{{- end }}

{{- define "carrefourconnect.datasourceUsername" -}}
{{- if .Values.postgis.enabled }}
{{- .Values.postgis.username }}
{{- else }}
{{- required "externalDatabase.username est obligatoire quand postgis.enabled=false" .Values.externalDatabase.username }}
{{- end }}
{{- end }}
