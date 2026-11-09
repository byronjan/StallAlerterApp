# Stall Alerter — Implementation Plan

## Overview

Five‑day schedule to build the Stall Alerter Android application. Each day has a primary goal and deliverables. All tasks follow the specification files in `specs/`.

---

## Day 1 — Project Setup & CSV Import Infrastructure

### Goal
Set up the Android Studio project with required conventions, implement CSV import via Storage Access Framework, and create the foundational data models.

### Tasks

1. **Initialize project structure** conforming to conventions:
   - Create `specs/`, `tasks/`, `logs/`, `reflection/` directories ✅ (already created)
   - Add `intent.md`, `Final directive.md` ✅ (already created)
   - Create `README.md` with project overview

2. **Define CSV data model** (`data/csv/Candidate.kt`):
   - Data class with `fullName: String`, `stage: Stage`, `lastContact: LocalDate`, `email: String?`, `phone: String?`
   - `Stage` enum class with values: `screening`, `technicalInterview`, `onSite`, `offer`, `rejected`, `hired`
   - Companion/object for parsing column names from CSV header

3. **Create CSV parser** (`repo/CsvImportRepo.kt`):
   - Function `importCsv(Uri): Result<List<Candidate>, ImportError`
   - Uses Android `ContentResolver` to read CSV bytes from the selected Uri
   - Parses header row, validates required columns (`full_name`, `stage`, `last_contact`)
   - Skips rows with invalid data (warnings, not errors)
   - Returns `ImportSuccess` with list or `ImportFailed` with error message

4. **Create Repository abstraction** (`repo/CsvRepository.kt`):
   - Interface `CsvRepository` with `importCsv()` method
   - Implementation delegates to `CsvImportRepo`

5. **Basic Compose UI** (`ui/ImportScreen.kt`):
   - Button "Import CSV" → opens Storage Access Framework (`ACTION_OPEN_DOCUMENT`) for `.csv`
   - Shows result: success count, error message, or "No data found"
   - On successful import, navigates to candidate list (or refreshes)

6. **Unit tests** for CSV parser:
   - Test valid CSV parsing
   - Test missing required columns
   - Test invalid date skipping
   - Test unknown stage skipping

### Deliverables end of Day 1
- `specs/csv-schema.md` ✅
- `data/csv/Candidate.kt`
- `repo/CsvImportRepo.kt` + `repo/CsvRepository.kt`
- `ui/ImportScreen.kt`
- `logs/day-1.md`
- Unit tests passing

### Risks
- Storage Access Framework permission handling on Android 11+ (Scoped Storage)
- CSV parsing edge cases (BOM, CRLF line endings, extra whitespace)
- Material 3 components not yet wired

---

## Day 2 — Priority Calculation & Flag Logic

### Goal
Implement the priority scoring model and flag computation as specified.

### Tasks

1. **Create `PriorityCalculator` ViewModel** (`ui/observable/PriorityCalculatorViewModel.kt`):
   - Takes `CsvRepository` via constructor injection
   - Exposes `StateFlow<List<CandidateFlags>>` where `CandidateFlags` = `Candidate` + `List<Flag>` + `priorityTier: PriorityTier`
   - Computes `daysSinceLastContact` using injected `Clock`
   - Applies stage weights and score formula from `specs/priority-model.md`

2. **Implement flag engine** (`business/flags/FlagEngine.kt`):
   - Function `computeFlags(candidate: Candidate, daysSinceLastContact: Int): List<Flag>`
   - Implements all flag conditions from `specs/flag-criteria.md`
   - Handles `RECENT_ACTIVITY` overriding stale flags
   - Handles `HIRED_STALE` taking precedence
   - Returns list of flag objects with user‑facing description and `flagId`

3. **Define `PriorityTier` enum** (`model/PriorityTier.kt`):
   - Values: `Critical`, `High`, `Medium`, `Low`, `None`
   - Mapping from score to tier (per `specs/priority-model.md` tiers table)

4. **Add `Flag` data class** (`model/Flag.kt`):
   - `flagId: FlagId` (enum or sealed class)
   - `description: String` (user‑facing)
   - `scoreAdjustment: Int` (how much it changes the priority score)

5. **Compose UI: Candidate list screen** (`ui/CandidateListScreen.kt`):
   - Shows list of candidates with: name, stage, last contact date, priority badge (tier + color), flag icons
   - Sorts by priority score (highest first)
   - Each item tap → detail screen (see Day 3)

6. **Unit tests** for:
   - `PriorityCalculator` with fixed Clock (deterministic scores)
   - `FlagEngine` all flag types with various candidate states
   - Score → tier mapping

### Deliverables end of Day 2
- `ui/observable/PriorityCalculatorViewModel.kt`
- `business/flags/FlagEngine.kt`
- `model/PriorityTier.kt`
- `model/Flag.kt`
- `ui/CandidateListScreen.kt`
- `logs/day-2.md`
- All unit tests passing

### Risks
- Flag overlap logic (e.g., STALE_60 also implies STALE_30 and STALE_14 — need to avoid double‑counting or decide if they should be)
- Score adjustment cumulativity — need to ensure flag adjustments are applied correctly relative to base score
- Clock injection pattern in ViewModel

---

## Day 3 — Next‑Action Recommendations & Detail Screen

### Goal
Add the "recommended next action" feature and detail screen for individual candidates.

### Tasks

1. **Create `NextActionRecommender`** (`business/actions/NextActionRecommender.kt`):
   - Function `recommendNextAction(candidate: Candidate, flags: List<Flag>): NextAction`
   - Uses candidate stage + flags to determine action
   - Returns `NextAction` data class with: `title: String`, `subtitle: String?`, `actionId: ActionId`

2. **Define `NextAction` and `ActionId`** (`model/NextAction.kt`):
   - Predefined actions per stage:
     - `screening` → "Send follow‑up email", "Schedule phone screen"
     - `technical-interview` → "Send technical assignment", "Schedule follow‑up interview"
     - `on-site` → "Send thank‑you note", "Schedule on‑site follow‑up"
     - `offer` → "Escalate to hiring manager", "Extend offer deadline"
     - `rejected` / `hired` → "No action needed"
   - Actions also consider flags (e.g., `OFFER_STALE` → "Escalate to hiring manager")

3. **Create `CandidateDetailScreen`** (`ui/CandidateDetailScreen.kt`):
   - Shows candidate full info: name, stage, last contact, all flags, priority tier
   - Shows recommended next action at the bottom
   - "Refresh" button to re‑import or recompute
   - Share button to export candidate info

4. **Update ViewModel** to pass selected candidate's `CandidateFlags` to the detail screen via `StateFlow`.

5. **Unit tests** for `NextActionRecommender`:
   - Each stage produces appropriate action
   - FLAG_OFFER_STALE → escalation action
   - FLAG_HIRED_STALE → "No action needed"

### Deliverables end of Day 3
- `business/actions/NextActionRecommender.kt`
- `model/NextAction.kt` + `model/ActionId.kt`
- `ui/CandidateDetailScreen.kt`
- Updated ViewModel with candidate detail state
- `logs/day-3.md`
- All unit tests passing

### Risks
- Action titles must be concise and fit Material 3 button constraints
- Sharing intent on Android (needs proper Intent construction)

---

## Day 4 — Polish, Edge Cases, and Verification

### Goal
Polish the UI, handle edge cases, run full verification, and ensure build passes.

### Tasks

1. **Material 3 theming** — ensure priority tier colors use `ColorScheme` from theme:
   - `Critical` → `error` or `colorRed600`
   - `High` → `warning` or `colorOrange600`
   - `Medium` → `secondary` or `colorAmber600`
   - `Low` → `surface` or `colorTeal500`
   - `None` → `onSurface` or `colorGray600`

2. **Error handling UI** — improve `ImportScreen` with:
   - Progress indicator during CSV read
   - "Retry" button on failure
   - Toast notifications for warnings (rows skipped)

3. **Handle Storage Access Framework lifecycle**:
   - Properly request `READ_EXTERNAL_STORAGE` or use `SERIALIZE` flag if needed
   - Handle user cancellation of file picker
   - Handle MIME type filtering for `.csv`

4. **Add `logs/day-4.md`** documenting:
   - Any issues encountered during Days 1‑3
   - How they were resolved
   - Commands run and results

5. **Full build verification**:
   - `./gradlew assembleDebug` — must succeed
   - `./gradlew test` — all unit tests pass
   - `./gradlew connectedAndroidTest` — if device/emulator available, run instrumented tests

6. **README.md** update with:
   - Project overview
   - How to build and run
   - How to import a CSV
   - Design decisions reference

### Deliverables end of Day 4
- Polished UI with Material 3 colors
- Updated `logs/day-4.md`
- `README.md` with build instructions
- `./gradlew assembleDebug` succeeds
- `./gradlew test` all unit tests pass

### Risks
- Device/emulator availability for instrumented tests
- Material 3 color theming consistency across light/dark modes

---

## Day 5 — Finalization and Reflection

### Goal
Finalize all deliverables, run comprehensive verification, and write the unlearning reflection.

### Tasks

1. **Generate final build artifact** — `./gradlew assembleRelease` if possible, otherwise confirm `assembleDebug` is the final build.

2. **Ensure all deliverables exist and are correct**:
   - `intent.md` ✅
   - `Final directive.md` ✅
   - `README.md` ✅ (updated Day 4)
   - `specs/` all spec files ✅
   - `tasks/implementation-plan.md` ✅
   - `logs/day-1.md` through `logs/day-5.md` ✅
   - `reflection/what-i-unlearned.md`

3. **Write `reflection/what-i-unlearned.md`**:
   - Capture 3‑5 non‑obvious insights about Android/Compose/CSV handling
   - What assumptions were surprised‑replaced
   - Changes in working habits due to AI‑first workflow

4. **Final verification**:
   - Run `./gradlew test` — confirm 100% of unit tests pass
   - Run `./gradlew assembleDebug` — confirm debug build succeeds
   - Review all spec files for consistency with each other and with the code

5. **Submit / present** — the user will indicate next steps (e.g., review, demo, submission)

### Deliverables end of Day 5
- `reflection/what-i-unlearned.md`
- All unit tests passing
- Debug build successful
- All assignment deliverables present

### Risks
- Falling behind on the 5‑day schedule
- Missing a specification detail that causes rework
- Forgetting to document something in the logs/reflection

---