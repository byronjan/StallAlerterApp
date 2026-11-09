# Stall Alerter — Intent

## Goal

Build an Android application using Kotlin and Jetpack Comprise that imports a hiring-board CSV file, identifies candidates who have not progressed, prioritizes follow‑ups, explains each flag, and recommends the next action.

## Core Features

1. **CSV Import** — User selects a CSV file via the Android Storage Access Framework. The CSV contains candidate data with columns for name, status, last interaction date, and other hiring‑board fields.

2. **Progress Identification** — After import, the app identifies candidates who have not progressed (stagnant, no recent activity, or stuck in early stages).

3. **Prioritization** — Candidates are ranked by urgency/compliance need using deterministic date calculations.

4. **Flag Explanations** — Each flagged candidate receives a clear explanation of why they were flagged (e.g., "No contact in 14 days", "Stuck at screening stage for 30+ days").

5. **Next‑Action Recommendations** — For each flagged candidate, the app recommends a specific next action (e.g., "Send follow‑up email", "Schedule interview", "Escalate to manager").

## User Flow

1. User taps "Import CSV" → system opens Storage Access Framework picker.
2. User selects a `.csv` file → data is parsed and displayed in a list.
3. App computes progress flags and priority scores.
4. List shows each candidate with: name, status, flag explanation, priority badge, and recommended action.
5. User can tap a candidate for details or refresh the import.

## Constraints

- **No backend, no database** — all processing is local (pure Kotlin business rules).
- **CSV import only through Android's Storage Access Framework** — no direct file system access.
- **Deterministic date calculations** using an injected `Clock` or `LocalDate` so results are reproducible.
- **MVVM architecture** with `ViewModel` and `StateFlow`.
- **Repository abstraction** over the CSV data source.
- **Constructor injection** for all dependencies.
- **Avoid unnecessary dependencies** — no databases, no networking, no backend services.
- **Unit tests** for business logic and CSV parsing.
- **Essential Compose UI tests** (essential only, not exhaustive).

## Success Criteria

- CSV can be imported via Storage Access Framework without crashing.
- Progress flags are computed correctly for all candidates.
- Prioritization ordering is deterministic and matches the specification.
- Each flag has a human‑readable explanation.
- Recommended next action is logical given the candidate's state.
- All UI is built with Jetpack Compose + Material 3.
- No native dependencies beyond the Android SDK and listed libraries.