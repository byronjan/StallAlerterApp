# Flag Criteria Specification

## Overview

Each candidate may receive one or more flags based on their data. A flag is a discrete explanation of why a candidate is not progressing. Flags are computed after the CSV is imported and the priority score is calculated.

## Flag Types

| Flag ID | Condition | Description (user‑facing) | Priority Tier Impact |
|---|---|---|---|
| `STALE_14` | `days_since_last_contact >= 14` and stage in (`screening`, `technical-interview`) | "No contact in 14+ days" | Adds 5 to priority score |
| `STALE_30` | `days_since_last_contact >= 30` and stage in (`screening`, `technical-interview`, `on-site`) | "No contact in 30+ days" | Adds 10 to priority score |
| `STALE_60` | `days_since_last_contact >= 60` | "No contact in 60+ days" | Adds 15 to priority score |
| `STUCK_SCREENING` | `stage == "screening"` and `days_since_last_contact >= 21` | "Stuck in screening for 21+ days" | Adds 8 to priority score |
| `STUCK_TECH` | `stage == "technical-interview"` and `days_since_last_contact >= 35` | "Stuck in technical interview for 35+ days" | Adds 12 to priority score |
| `RECENT_ACTIVITY` | `days_since_last_contact < 7` | "Recent activity — no follow‑up needed" | Removes 5 from priority score (lowers priority) |
| `OFFER_STALE` | `stage == "offer"` and `days_since_last_contact >= 14` | "Offer pending for 14+ days — escalate" | Adds 20 to priority score |
| `HIRED_STALE` | `stage == "hired"` and `days_since_last_contact >= 30` | "Hired candidate — no action needed" | Sets priority to "None" tier (bottom) |

## Flag Computation Rules

1. **Multiple flags per candidate** — a single candidate can have several flags enabled simultaneously (e.g., both `STALE_30` and `STUCK_SCREENING`).
2. **Flags are cumulative** — the priority score adjustments from all applicable flags are summed and applied to the base score.
3. **`RECENT_ACTIVITY` overrides stale flags** — if `days_since_last_contact < 7`, no `STALE_*`, `STUCK_*,` or `OFFER_STALE` flags are shown, but `RECENT_ACTIVITY` is displayed instead.
4. **`HIRED_STALE` takes precedence** — any candidate with `stage == "hired"` always gets the `HIRED_STALE` flag and is moved to the "None" priority tier regardless of score.
5. **`OFFER_STALE` only for offer stage** — the `OFFER_STALE` flag only applies when `stage == "offer"`.

## Flag Display Order

When displaying flags for a candidate, show them in this order:
1. `HIRED_STALE` (if applicable)
2. `STALE_60`, `STALE_30`, `STALE_14` (highest to lowest threshold)
3. `STUCK_SCREENING`, `STUCK_TECH`
4. `OFFER_STALE` (if applicable)
5. `RECENT_ACTIVITY` (if applicable, appears at top when present)

## Example

A candidate with:
- `stage = "screening"`
- `last_contact = 2025-01-05` (today is 2026-11-04 → 644 days ago)

Would receive flags:
- `STALE_60` (644 >= 60)
- `STALE_30` (644 >= 30)
- `STALE_14` (644 >= 14)
- `STUCK_SCREENING` (stage == screening and 644 >= 21)

Priority score adjustment: +15 + 10 + 5 + 8 = +38 added to base score.