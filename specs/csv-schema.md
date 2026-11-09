# CSV Schema Specification

## Overview

The hiring-board CSV file imported by Stall Alerter must conform to this schema. The app expects a header row followed by one row per candidate.

## Required Columns (in any order)

| Column Name | Type | Description |
|---|---|---|
| `full_name` | `String` | Candidate's full name |
| `stage` | `String` | Current hiring stage (e.g., "screening", "technical-interview", "offer", "rejected", "hired") |
| `last_contact` | `LocalDate` | Date of last contact/interaction with the candidate, format: `yyyy-MM-dd` |
| `email` | `String` | Candidate email (optional but recommended) |
| `phone` | `String` | Candidate phone (optional) |

## Optional Columns

| Column Name | Type | Description |
|---|---|---|
| `date_added` | `LocalDate` | When the candidate was added to the hiring board |
| `notes` | `String` | Free‑form notes about the candidate |
| `recruiter` | `String` | Name of the assigned recruiter |

## Schema Validation Rules

1. **Header row is required** — the first line must contain column names.
2. **`full_name` and `stage` are mandatory** — if missing, the entire import must fail with a clear error.
3. **`last_contact` format must be `yyyy-MM-dd`** — invalid dates cause that row to be skipped with a warning.
4. **`stage` values must be one of**: `screening`, `technical-interview`, `on-site`, `offer`, `rejected`, `hired`.
5. **Duplicate `full_name` entries are allowed** — the app treats each row independently.

## Error Handling

| Situation | App Behavior |
|---|---|
| Missing required columns | Show `ImportFailedError` dialog with message "CSV missing required columns: full_name, stage, last_contact" |
| Invalid `last_contact` date | Skip that row, show warning in UI: "Row for {name}: invalid last_contact date, skipping" |
| Unknown `stage` value | Skip that row, show warning: "Row for {name}: unknown stage '{value}'" |
| Empty file / no rows | Show info: "No candidate data found in CSV" |

## Example Valid CSV

```csv
full_name,stage,last_contact,email,phone
Alice Johnson,screening,2025-01-15,alice@example.com,555-1234
Bob Smith,technical-interview,2025-02-20,bob@example.com,555-5678
Charlie Brown,offer,2025-03-01,charlie@example.com,555-9012
```