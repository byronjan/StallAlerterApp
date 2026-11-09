# Priority Model Specification

## Overview

Candidates who have not progressed are prioritized using a deterministic scoring formula. The score determines the order in which candidates appear in the UI (highest score = highest priority).

## Scoring Formula

The priority score is computed as:

```
score = days_since_last_contact + stage_weight
```

Where:

- **`days_since_last_contact`** = `LocalDate.now(clock).toDate().until(last_contact).days`
  - This is the number of days since the candidate had last contact. Larger values = more stale = higher priority.
  - Uses the injected `Clock` so calculations are deterministic in tests.
  
- **`stage_weight`** is a fixed per‑stage offset:

| Stage | Weight |
|---|---|
| `screening` | 0 |
| `technical-interview` | 10 |
| `on-site` | 20 |
| `offer` | 30 |
| `rejected` | -10 (these go to the bottom) |
| `hired` | -20 (these go to the bottom) |

## Priority Tiers

| Score Range | Tier | UI Label |
|---|---|---|
| 60+ | Critical | 🔴 Critical |
| 30‑59 | High | 🟠 High |
| 10‑29 | Medium | 🟡 Medium |
| 0‑9 | Low | 🟢 Low |
| < 0 | None | ⚪ None (rejected/hired) |

## Deterministic Behavior

- The `Clock` dependency is injected into the `PriorityCalculator` ViewModel.
- In production, the clock is `System.clock()` (or `LocalDate.now()`).
- In tests, a fixed `FixedClock` can be injected so that `days_since_last_contact` is predictable.
- Score ordering is **stable**: if two candidates have the same score, they are ordered by `last_contact` ascending (oldest first), then by `full_name` alphabetically.

## Example Calculations

| Candidate | last_contact | days_since | stage_weight | score | Tier |
|---|---|---|---|---|---|
| Alice | 2025-01-01 | 277 | 0 | 277 | Critical |
| Bob | 2025-02-15 | 192 | 10 | 202 | Critical |
| Charlie | 2025-03-01 | 173 | 20 | 193 | Critical |
| Diana | 2025-01-20 | 264 | 0 | 264 | Critical |
| Evan | 2025-05-01 | 157 | 30 | 187 | Critical |
| Fiona | 2025-06-01 | 123 | 30 | 153 | High |
| Greg | 2025-07-01 | 92 | 20 | 112 | Medium |
| Hank | 2025-08-01 | 62 | 0 | 62 | Low |
| Irene | 2025-09-01 | 31 | 0 | 31 | Low |
| Jack | 2025-10-01 | 4 | 0 | 4 | None (rejected) |