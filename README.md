# TCG-Manager

Android app to scan trading cards (Magic: The Gathering, Pokémon TCG, …), look them up in online card databases and keep track of your collection.

## Features

- **Scan a card** with the camera. ML Kit reads the text on-device, guesses which game it is and finds the card name.
- **Manual search** by ID, name or description.
- **Multiple card APIs.** Each API declares which searches it supports; APIs without image search fall back to OCR + name search.
- **Card details**: stats, rules text, flavour text, prices, artist and source.
- **Collection**: after a search the app asks whether to add the card. Cards are stored locally with all received data and the number of copies owned.

## Supported games

| Game | API | ID | Name | Description | Image |
|---|---|:-:|:-:|:-:|:-:|
| Magic: The Gathering | [Scryfall](https://scryfall.com/docs/api) | ✅ | ✅ (fuzzy) | ✅ (rules text) | OCR fallback |
| Pokémon TCG | [pokemontcg.io](https://docs.pokemontcg.io) | ✅ | ✅ | ✅ (flavour / ability text) | OCR fallback |

Neither API needs a key. pokemontcg.io can be unreliable (intermittent 5xx); the app retries failed requests automatically.

## Requirements

- Android Studio (recent version with AGP 9.5 support) and JDK 17+
- Android 7.0 (API 24) or newer device/emulator
- Camera for scanning (optional: manual search works without one)
- Internet connection for card searches (OCR runs offline)

## Build and run

Open the project in Android Studio and press **Run**, or from a terminal:

```bash
./gradlew assembleDebug        # build the debug APK
./gradlew installDebug         # install on a connected device
./gradlew testDebugUnitTest    # run unit tests
```

## Project structure

```
app/src/main/java/com/example/tcg_manager/
├── TcgManagerApp.kt          Application class
├── AppContainer.kt           creates database, APIs and repositories (manual DI)
├── data/
│   ├── model/                Card, CollectionCard, CardGame, SearchType
│   ├── local/
│   │   ├── db/               AppDatabase (Room), type converters
│   │   ├── entity/           CardEntity + mappers
│   │   └── dao/              CardDao
│   ├── remote/               CardApiProvider interface, registry, HTTP setup
│   │   ├── scryfall/         Magic: The Gathering
│   │   └── pokemon/          Pokémon TCG
│   └── repository/           CardSearchRepository, CollectionRepository
├── scanner/                  ML Kit OCR, game detection, name extraction
├── ui/
│   ├── MainActivity.kt
│   ├── scan/                 camera + manual search
│   ├── card/                 card details + "add to collection?"
│   └── collection/           saved cards with quantities
└── util/                     JSON and bitmap helpers
app/schemas/                  exported Room schemas (keep in git for migrations)
```

### Data flow

```
Camera / search box → ScanViewModel → CardSearchRepository → CardApiProvider(s) → Card
Card → CardDetailFragment → CollectionRepository → Room (cards table)
```

### Database

A single `cards` table, keyed by `(source, externalId)`. It stores the common card fields, game-specific `stats` and `prices` (as JSON), the complete API response in `rawJson`, and the owned `quantity`. Adding a card that is already saved refreshes its data and increases the quantity.

## Adding another game or API

1. Add a value to `data/model/CardGame.kt`.
2. Create a provider in `data/remote/<api>/` implementing `CardApiProvider`. Set `capabilities` and override only the searches the API supports.
3. Register it in `AppContainer.apiRegistry`.
4. Add keywords for the game to `scanner/CardGameDetector.kt` so scans can detect it.

## Tech stack

Kotlin · Views + ViewBinding · Navigation · ViewModel + coroutines/Flow · Room (KSP) · Retrofit + Gson · CameraX · ML Kit Text Recognition

## Known limitations

- Card images are not displayed yet (the URL is saved).
- Name detection from scans is heuristic: good light and the card filling the frame give the best results.
- The bundled ML Kit model makes the debug APK about 54 MB.
