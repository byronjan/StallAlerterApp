# Day 2 — Priority Calculation & Flag Logic

## Goals Achieved
- Implemented `PriorityCalculator` — pure Kotlin deterministic scoring with injected `Clock`
- Implemented `FlagEngine` — all 8 flag types with precedence rules from specs
- Created `Flag` model (`FlagId` enum, `Flag` data class with description and scoreAdjustment)
- Created `PriorityTier` enum + `PriorityTierMapper.fromScore()` with spec-defined ranges
- Created `CandidateViewModel` — MVVM with `StateFlow`, constructor injection of `CsvRepository` + `Clock`
- Created `CandidateUiState` and `CandidateListUiState` sealed interface
- Added required dependencies: `lifecycle-viewmodel-ktx`, `kotlinx-coroutines-android`
- Comprehensive unit tests for both `PriorityCalculator` and `FlagEngine` (21 tests passing)

## Implementation Details

### PriorityCalculator (`business/PriorityCalculator.kt`)
- Formula: `score = daysSinceLastContact + stageWeight`
- Stage weights per spec: screening=0, technicalInterview=10, onSite=20, offer=30, rejected=-10, hired=-20
- Uses `ChronoUnit.DAYS.between()` for correct day counting
- Injected `Clock` for deterministic testing

### FlagEngine (`business/flags/FlagEngine.kt`)
- Implements all 8 flags from `specs/flag-criteria.md`:
  - `STALE_14` (days ≥ 14, screening/technical): +5
  - `STALE_30` (days ≥ 30, screening/technical/onSite): +10
  - `STALE_60` (days ≥ 60, all): +15
  - `STUCK_SCREENING` (screening, days ≥ 21): +8
  - `STUCK_TECH` (technicalInterview, days ≥ 35): +12
  - `RECENT_ACTIVITY` (days < 7): -5 (overrides all stale/stuck/offer)
  - `OFFER_STALE` (offer, days ≥ 14): +20
  - `HIRED_STALE` (hired, days ≥ 30): tier = None (replaces all)
- Flags are cumulative (e.g., days ≥ 60 gets all three STALE flags)
- Returns flags in spec display order

### PriorityTier (`model/PriorityTier.kt`)
- Tiers per spec: Critical (≥60), High (30-59), Medium (10-29), Low (0-9), None (<0)

### CandidateViewModel (`viewmodel/CandidateViewModel.kt`)
- Holds `CsvRepository` + `Clock` via constructor injection
- Exposes `StateFlow<CandidateListUiState>` (Loading, Success, Error, Empty)
- `onCandidatesParsed(candidates)` — computes flags, scores, tiers, sorts
- Sorting: score desc → lastContact asc → fullName asc
- HIRED_STALE forces tier = None regardless of score

## Commands Run
- `./gradlew test` — all 21 unit tests passing ✅
- `./gradlew assembleDebug` — debug build successful ✅

## Issues & Resolutions
- **Period.getDays() vs ChronoUnit.DAYS.between()**: `Period.until()` returns a Period object where `getDays()` only gives the days component (not total days). Fixed by using `ChronoUnit.DAYS.between(start, end)` which returns total days as a long.
- **Clock time zone handling**: Fixed clock must use system default zone to match `LocalDate.now(Clock)` behavior. Used `LocalDate.atStartOfDay(ZoneId.systemDefault())` when creating the fixed instant.
- **Off-by-one in test expectations**: Corrected expected day counts after fixing the calculation (672, 627, 613, etc.)

## Learnings
- `Period` vs `ChronoUnit` distinction is critical for date arithmetic
- Fixed clocks need careful zone handling to match `LocalDate.now()` behavior
- Specification-driven development catches edge cases early (cumulative flags, precedence rules)
- Pure Kotlin business logic with injected dependencies enables fast, deterministic unit tests

## Next Steps (Day 3)
- Create `NextActionRecommender` business logic
- Create `CandidateDetailScreen` Compose UI
- Update ViewModel to pass selected candidate detail state
- Unit tests for NextActionRecommender