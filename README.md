# Stall Alerter

Android application (Kotlin + Jetpack Compose) that imports a hiring-board CSV, identifies stalled candidates, prioritizes follow-ups, explains flags, and recommends next actions.

## Project Structure

```
StallAlerterApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/
│   │   │   │   └── com/example/stallalerterapp/
│   │   │   │     ├── model/            # Data models (Candidate, Flag, NextAction, PriorityTier)
│   │   │   │     ├── business/         # Pure Kotlin business logic
│   │   │   │     │   ├── PriorityCalculator.kt        # Deterministic scoring
│   │   │   │     │   ├── flags/FlagEngine.kt           # 8 flag types
│   │   │   │     │   └── actions/NextActionRecommender.kt  # Next action recommendations
│   │   │   │     ├── data/csv/                    # CSV parsing (Candidate, ImportError, CsvParser)
│   │   │   │     ├── repo/                        # Repository abstraction (CsvRepository interface, CsvImportRepo)
│   │   │   │     ├── viewmodel/                   # MVVM layer (CandidateViewModel, CandidateUiState)
│   │   │   │     └── ui/                        # Compose UI screens
│   │   │   │         ├── CandidateListScreen.kt      # Candidate list with priority sorting
│   │   │   │         ├── CandidateDetailScreen.kt    # Detailed candidate view
│   │   │   │         └── theme/Theme.kt              # Material 3 theming
│   │   │   │     └── MainActivity.kt                 # Entry point Compose UI
│   │   │   ├── res/                        # Android resources (values, drawable, mipmap)
│   │   │   └── AndroidManifest.xml         # App manifest with activity declaration
│   │   ├── test/                           # Unit tests
│   │   └── androidTest/                    # Instrumented tests
│   ├── build.gradle.kts                    # Build config with explicit Compose compiler plugin & lifecycle pin
│   └── gradle/
│       └── libs.versions.toml              # Version catalog (AGP 9.5.0, Kotlin 2.2.10, Compose BOM 2024.08.00)
├── specs/
│   ├── csv-schema.md          # CSV import schema specification
│   ├── flag-criteria.md       # Flag computation rules
│   └── priority-model.md      # Priority scoring model
├── tasks/
│   └── implementation-plan.md # Five-day dependency-ordered plan
├── logs/
│   ├── day-1.md               # Day 1: Project setup & CSV import infrastructure
│   ├── day-2.md               # Day 2: Priority calculation & flag logic
│   ├── day-3.md               # Day 3: Next-action recommendations & detail screen
│   ├── day-4.md               # Day 4: Material 3 theming, navigation, CSV import UI
│   └── day-5.md               # Day 5: Finalization and reflection
└── reflection/
    └── what-i-unlearned.md    # Reflection on non-obvious insights
```

## Core Features

1. **CSV Import** — User selects a CSV file via the Android Storage Access Framework (ACTION_OPEN_DOCUMENT with `.csv` MIME type). Parser validates required columns (`full_name`, `stage`, `last_contact`) and handles invalid data gracefully.

2. **Progress Identification** — After import, candidates are ranked by priority score using deterministic date calculations.

3. **Prioritization** — Candidates ranked by adjusted priority score (higher = more urgent). Sorting order: adjustedScore descending, lastContact ascending, fullName ascending.

4. **Flag Explanations** — Each candidate may receive one or more flags explaining why they were flagged (e.g., "No contact in 14 days", "Stuck in screening for 21+ days", "Offer pending for 14+ days — escalate").

5. **Next-Action Recommendations** — For each candidate, a recommended next action based on stage and flags (e.g., "Send follow-up email", "Escalate to hiring manager", "No action needed").

## Building and Running

### Prerequisites

- Android Studio Flamingo (or newer)
- JDK 17
- Android SDK 37 (target) / SDK 24 (minimum)

### Building

```bash
# Clean build
./gradlew clean assembleDebug

# Run unit tests
./gradlew test

# Full verification
./gradlew clean assembleDebug test
```

### Key Build Configuration

The project uses AGP 9.5.0 with Kotlin 2.2.10. The explicit Compose compiler plugin is required to avoid "couldn't find inline method" errors:

```kotlin
// app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10"
}

configurations.configureEach {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "androidx.lifecycle") {
                useVersion("2.8.7")
            }
        }
    }
}
```

### Version Catalog (gradle/libs.versions.toml)

```
[versions]
agp = "9.5.0"
coreKtx = "1.16.0"
appcompat = "1.8.0"
material = "1.14.0"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"
kotlin = "2.2.10"
composeBom = "2024.08.00"
composeUi = "1.9.0"
composeMaterial3 = "1.3.0"
composeFoundation = "1.9.0"
activityCompose = "1.9.0"
composeMaterialIconsCore = "1.7.0"
composeMaterialIconsExtended = "1.7.0"
navigationCompose = "2.8.0"
lifecycleViewModelCompose = "2.8.7"
lifecycleViewModelKtx = "2.8.0"
lifecycleRuntimeCompose = "2.8.0"
lifecycleVersion = "2.8.0"
kotlinxCoroutinesAndroid = "1.8.0"
kotlinxCoroutinesTest = "1.8.0"
archCoreTesting = "2.2.0"
robolectric = "4.11"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
material = { group = "com.google.android.material", name = "material", version.ref = "material" }
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
compose-ui = { group = "androidx.compose.ui", name = "ui", version.ref = "composeUi" }
compose-material3 = { group = "androidx.compose.material3", name = "material3", version.ref = "composeMaterial3" }
compose-foundation = { group = "androidx.compose.foundation", name = "foundation", version.ref = "composeFoundation" }
compose-material-icons-core = { group = "androidx.compose.material", name = "material-icons-core", version.ref = "composeMaterialIconsCore" }
compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended", version.ref = "composeMaterialIconsExtended" }
activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleViewModelCompose" }
lifecycle-viewmodel-ktx = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-ktx", version.ref = "lifecycleViewModelKtx" }
lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycleRuntimeCompose" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "kotlinxCoroutinesAndroid" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "kotlinxCoroutinesTest" }
arch-core-testing = { group = "androidx.arch.core", name = "core-testing", version.ref = "archCoreTesting" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
```

## Design Decisions

- **Pure Kotlin business logic** with constructor injection (Clock for determinism)
- **Lightweight MVVM** with ViewModel + StateFlow, Repository abstraction
- **No databases, backend, or speculative features** — all processing is local
- **CSV import only through Android's Storage Access Framework** — no direct file system access
- **Deterministic date calculations** using an injected Clock so results are reproducible
- **Material 3 theming** with priority tier colors tied to ColorScheme
- **Essential Compose UI tests** only — not exhaustive
- **Navigation workaround**: `navigation-compose` was added to the version catalog but not used in practice; screens are hosted directly in a single Activity with explicit `navController.navigate()` calls, avoiding a library version conflict that surfaced during development
- **AGP 9.5.0 + Kotlin 2.2.10 + lifecycle 2.8.7 pin**: The `androidx.lifecycle` group is force-pinned to 2.8.7 via `configurations.configureEach { resolutionStrategy { eachDependency { if (requested.group == "androidx.lifecycle") { useVersion("2.8.7") } } } }` in `app/build.gradle.kts`. This is necessary because newer lifecycle releases (2.9+, 2.10) are compiled with a Kotlin version whose inline-function bytecode AGP 9.5's built-in Kotlin 2.2.10 cannot read — attempting `viewModel<T>()` causes an `IllegalStateException: couldn't find inline method` compiler crash. The pin was discovered after misdiagnosing the error as an import package problem and a Navigation Compose incompatibility; both were actually latent symptoms of the lifecycle version mismatch.
- **No `org.jetbrains.kotlin.android` plugin**: AGP 9.0+ has built-in Kotlin support and rejects the explicit Kotlin Android plugin. Applying it causes `"The 'org.jetbrains.kotlin.android' plugin is no longer required for Kotlin support since AGP 9.0"` error. The project uses AGP's built-in Kotlin (2.2.10) with the explicit Compose compiler plugin `org.jetbrains.kotlin.plugin.compose` version "2.2.10" to enable proper Compose compilation.

## Unit Tests

33 unit tests covering:

- **PriorityCalculatorTest** (7 tests) — deterministic scoring with injected Clock
- **FlagEngineTest** (13 tests) — all 8 flag types, cumulative adjustments, display order, spec examples
- **NextActionRecommenderTest** (12 tests) — all stage-based actions, flag overrides (HIRED_STALE, OFFER_STALE), determinism
- **ExampleUnitTest** (1 basic test)

All tests verified passing on Day 5: `./gradlew test` ✅

## CSV Import Schema

Required columns (in any order): `full_name`, `stage`, `last_contact`
Optional columns: `email`, `phone`, `date_added`, `notes`, `recruiter`

CSV format example:
```csv
full_name,stage,last_contact,email,phone
Alice Johnson,screening,2025-01-15,alice@example.com,555-1234
Bob Smith,technical-interview,2025-02-20,bob@example.com,555-5678
```

## License

Proprietary — AI-first, specification-driven 5-day assignment.