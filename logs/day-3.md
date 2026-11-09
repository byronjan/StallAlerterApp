# Day 3 Log — NextActionRecommender & CandidateDetailScreen

**Date:** 2026-11-05  
**Goal:** Implement recommended next action feature and candidate detail screen

---

## Summary

Day 3 completed successfully. All business logic (NextActionRecommender), ViewModel detail state, and CandidateDetailScreen Compose UI are implemented and compiling. All 33 unit tests pass (21 existing + 12 new). The main challenge was a Compose compiler internal error due to AGP 9.5.0 bundling Kotlin 2.2.10, which required explicitly applying the Compose compiler plugin.

---

## What Was Built

### 1. `model/NextAction.kt` (NEW)
- `ActionId` enum with 9 values:
  - `SEND_FOLLOWUP_EMAIL`, `SCHEDULE_PHONE_SCREEN`
  - `SEND_TECHNICAL_ASSIGNMENT`, `SCHEDULE_FOLLOWUP_INTERVIEW`
  - `SEND_THANK_YOU_NOTE`, `SCHEDULE_ONSITE_FOLLOWUP`
  - `ESCALATE_TO_HIRING_MANAGER`, `EXTEND_OFFER_DEADLINE`
  - `NO_ACTION_NEEDED`
- `NextAction` data class with `title`, optional `subtitle`, and `actionId`

### 2. `business/actions/NextActionRecommender.kt` (NEW)
- Pure function `recommendNextAction(Candidate, List<Flag>): NextAction`
- Precedence logic:
  1. `HIRED_STALE` flag → `NO_ACTION_NEEDED` (absolute precedence)
  2. `OFFER_STALE` flag → `ESCALATE_TO_HIRING_MANAGER` (overrides stage)
  3. Stage-based defaults:
     - `screening` → `SEND_FOLLOWUP_EMAIL`
     - `technicalInterview` → `SEND_TECHNICAL_ASSIGNMENT`
     - `onSite` → `SEND_THANK_YOU_NOTE`
     - `offer` → `EXTEND_OFFER_DEADLINE`
     - `rejected`/`hired` → `NO_ACTION_NEEDED`

### 3. `viewmodel/CandidateViewModel.kt` (MODIFIED)
- Added `_detailState: MutableStateFlow<CandidateUiState?>` and `detailState: StateFlow`
- Added `_nextAction: MutableStateFlow<NextAction?>` and `nextAction: StateFlow`
- Added `selectCandidate(candidateUiState: CandidateUiState)` — computes and sets detail state + next action
- Added `clearSelection()` — clears detail state and next action

### 4. `ui/CandidateDetailScreen.kt` (NEW)
Full Material 3 Compose screen with:
- **TopAppBar**: Title, back button, share action
- **Header card**: Candidate name, stage badge, priority tier badge, email/phone, last contact date
- **Flags section**: All flags with color-coded icons and score adjustments
- **Score breakdown**: Base score, flag adjustments, adjusted score
- **Recommended action card**: Prominent card with action title, subtitle, and type badge

### 5. `test/.../business/actions/NextActionRecommenderTest.kt` (NEW)
12 deterministic tests covering:
- All 6 stage-based default actions
- `HIRED_STALE` override (highest precedence)
- `OFFER_STALE` override on offer and other stages
- Multiple flags with `HIRED_STALE` → `NO_ACTION_NEEDED`
- Multiple flags with `OFFER_STALE` but not `HIRED_STALE` → `ESCALATE_TO_HIRING_MANAGER`
- Determinism (same input → same output)

---

## Build Issues & Resolution

### Problem: Compose Compiler Internal Error
**Error:** `couldn't find inline method Landroidx/compose/foundation/layout/ColumnKt;.Column(...)V`

**Root Cause:** AGP 9.5.0 bundles Kotlin 2.2.10, but the Compose compiler plugin was not being explicitly applied. The `kotlinCompilerExtensionVersion` in `composeOptions` was being ignored/overridden.

**Resolution:** Explicitly apply the Compose compiler plugin in `app/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10"
}
```

This matches the Kotlin version bundled by AGP 9.5.0 (2.2.10) and enables proper Compose compilation.

### Version Alignment
Final working versions:
- AGP: 9.5.0
- Kotlin: 2.2.10 (bundled by AGP)
- Compose Compiler: 2.2.10 (explicit plugin)
- Compose BOM: 2024.08.00
- Compose UI: 1.7.0
- Material3: 1.3.0

---

## Verification Results

| Check | Result |
|-------|--------|
| `./gradlew clean assembleDebug` | ✅ SUCCESS |
| `./gradlew test` | ✅ SUCCESS (33 tests: 1+7+12+13) |
| NextActionRecommender tests | ✅ 12/12 pass |
| PriorityCalculator tests | ✅ 7/7 pass |
| FlagEngine tests | ✅ 13/13 pass |
| ExampleUnitTest | ✅ 1/1 pass |

---

## Files Created/Modified

| File | Status |
|------|--------|
| `app/src/main/kotlin/com/example/stallalerterapp/model/NextAction.kt` | CREATED |
| `app/src/main/kotlin/com/example/stallalerterapp/business/actions/NextActionRecommender.kt` | CREATED |
| `app/src/main/kotlin/com/example/stallalerterapp/viewmodel/CandidateViewModel.kt` | MODIFIED |
| `app/src/main/kotlin/com/example/stallalerterapp/ui/CandidateDetailScreen.kt` | CREATED |
| `app/src/test/java/com/example/stallalerterapp/business/actions/NextActionRecommenderTest.kt` | CREATED |
| `app/build.gradle.kts` | MODIFIED (added Compose compiler plugin) |
| `gradle/libs.versions.toml` | MODIFIED (version alignment) |

---

## Next Steps (Day 4)

1. Material 3 theming and polish
2. Navigation integration (list → detail)
3. CSV import UI (Storage Access Framework)
4. Error handling and empty states
5. Full verification and `assembleDebug`

---

## Reflection

**What worked well:**
- Specification-driven approach kept business logic clean and testable
- Pure Kotlin functions with no Android dependencies are trivial to test
- Explicit Compose compiler plugin resolved the AGP 9.5.0/Kotlin 2.2.10 mismatch

**What was challenging:**
- Spent significant time debugging the Compose internal compiler error
- The error message ("couldn't find inline method") was misleading — it wasn't a dependency issue but a compiler plugin version mismatch
- AGP 9.5.0 bundles its own Kotlin version, overriding the version catalog

**Lesson for Day 4:** Always apply Compose compiler plugin explicitly when using AGP that bundles Kotlin. Check `./gradlew :app:compileDebugKotlin --debug` for "Kotlin compiler classpath" to verify the plugin is loaded.