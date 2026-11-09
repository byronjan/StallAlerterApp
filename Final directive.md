# Stall Alerter — Final Directive

This document records the final decisions, unresolved questions, and closure state for the Stall Alerter assignment.

## Decisions Made

- **Specification-driven workflow**: All work will follow the specs/ directory as the source of truth. Each task will have an explicit spec file before implementation begins.
- **Five‑day deadline**: All deliverables must be completed within 5 calendar days from project start.
- **AI‑first but human‑verified**: Claude Code generates implementations, but the user approves every task and verifies tests/builds.
- **Minimal dependencies**: Only AndroidX, Material 3, and Kotlin stdlib. No databases, no networking, no custom backend.
- **CSV via Storage Access Framework**: Only method for importing candidate data.

## Open Questions / To‑Be‑Resolved

- Exact CSV column schema (to be defined in specs/csv-schema.md).
- Priority scoring formula (to be defined in specs/priority-model.md).
- Flag criteria thresholds (to be defined in specs/flag-criteria.md).

## Deliverables Checklist

- [x] intent.md
- [ ] Final directive.md (this file)
- [ ] README.md
- [ ] specs/ (all spec files)
- [ ] tasks/implementation-plan.md
- [ ] logs/day-1.md through logs/day-5.md
- [ ] reflection/what-i-unlearned.md

## Verification Notes

- All Gradle tasks and build variants must be discovered before implementation begins.
- Unit tests for CSV parsing and business logic must pass on every commit.
- Essential Compose UI tests must exercise the import → flag → prioritize flow.
- No runtime crashes on CSV import via Storage Access Framework.

## Unlearned (to be filled at end)

- what-i-unlearned.md will capture non‑obvious insights about Android/Compose/CSV handling that were surprising or required unlearning assumptions.