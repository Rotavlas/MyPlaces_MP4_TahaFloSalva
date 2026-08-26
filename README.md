# My Places — Le journal intime géographique

Application Android native (Kotlin / Jetpack Compose) — Mini-projet 4, LA MANU.

**Équipe :** Taha, Flo, Salva.

---

## Architecture

MVVM + Repository pattern :

```
app/
├── data/
│   ├── local/       → Room : PlaceEntity, PlaceDao, PlacesDatabase
│   ├── remote/      → Retrofit : GeocodingApi (api-adresse.data.gouv.fr)
│   └── repository/  → PlacesRepository
├── ui/
│   ├── screens/     → MapScreen, AddPlaceScreen, ListScreen, SettingsScreen, PlaceDetailSheet
│   ├── viewmodel/   → PlacesViewModel
│   └── theme/       → Theme.kt (Material 3)
└── utils/
    ├── ImportExportManager.kt
    └── BiometricHelper.kt
```

---

## Stack technique

| Composant | Choix |
|-----------|-------|
| UI | Jetpack Compose + Material 3 |
| Carte | OSMDroid (OpenStreetMap, sans clé API) |
| BDD locale | Room (SQLite) |
| Réseau | Retrofit 2 + Gson |
| Sécurité | BiometricPrompt + DataStore |
| Photo | CameraX + galerie |

---

## Lancer le projet

1. Ouvrir le dossier dans Android Studio (Ladybug ou plus récent)
2. Sync Gradle
3. Lancer sur émulateur ou device (minSdk 26)

Aucune clé API nécessaire — la carte utilise OpenStreetMap.

---

## Format du fichier d'échange (`places_export.json`)

```json
{
  "version": 1,
  "authorId": "me",
  "exportedAt": 1724443200000,
  "places": [
    {
      "title": "Café de Flore",
      "description": "Meilleur café de Paris",
      "emoji": "☕",
      "latitude": 48.8539,
      "longitude": 2.3325,
      "address": "172 Boulevard Saint-Germain, 75006 Paris",
      "photoPath": null,
      "timestamp": 1719900000000,
      "authorId": "me",
      "isOwn": true
    }
  ]
}
```

À l'import, chaque lieu reçoit `isOwn = false` et `authorId = nom de l'ami`, ce qui permet de distinguer ses propres lieux des lieux importés sans jamais écraser les données existantes.

---

## Fonctionnalités

- Carte OpenStreetMap plein écran + géolocalisation
- Ajout de lieu par clic long ou bouton +
- Marqueurs emoji personnalisés
- Formulaire : titre, description, emoji, photo (caméra ou galerie)
- Reverse geocoding automatique (api-adresse.data.gouv.fr)
- Persistance Room — photos en chemin interne, pas en Base64
- Export JSON + import depuis le fichier d'un ami
- Verrou biométrique optionnel
