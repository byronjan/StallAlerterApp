# Stall Alerter — Claude Code Instructions

## Project

Build Stall Alerter, an Android application using Kotlin and Jetpack Compose.

The application imports a hiring-board CSV, identifies candidates who have not progressed, prioritizes follow-ups, explains each flag, and recommends the next action.

The assignment must be completed in five days using an AI-first, specification-driven workflow.

## Approval policy

The user must approve every implementation task.

Before editing:
1. Read the relevant specifications and inspect the existing code.
2. Identify the task and its acceptance criteria.
3. Present the proposed files, implementation steps, tests, and risks.
4. Wait for explicit approval.

After implementing:
1. Inspect the actual changes.
2. Run the relevant tests and build commands.
3. Report actual results and remaining issues.
4. Wait for approval before beginning another task.

Do not treat approval of a plan as approval to implement every task.

Do not begin unrelated work.

## Source of truth

Follow these sources in order:

1. Latest explicit user instructions.
2. `Final directive.md`.
3. Approved specifications in `specs/`.
4. Approved implementation plan in `tasks/implementation-plan.md`.
5. Existing repository conventions.

Do not silently change requirements.

## Technical constraints

- Kotlin, Jetpack Compose, Material 3.
- Lightweight MVVM with ViewModel and StateFlow.
- Repository abstraction.
- Pure Kotlin business rules.
- Constructor injection.
- Deterministic date calculations using an injected date or Clock.
- CSV import through Android's Storage Access Framework.
- Unit tests for business logic and CSV parsing.
- Essential Compose UI tests.

Avoid unnecessary dependencies, frameworks, backend services, databases, and speculative features.

Inspect the existing project and use its established versions and conventions.

## Verification

Discover the actual Gradle tasks and build variants before selecting commands.

Use the repository's Gradle wrapper.

Run the narrowest relevant tests first, then the appropriate build.

Never claim a test or build passed unless it actually ran successfully.

Do not weaken or remove tests to hide failures.

If a command cannot run, report it as unverified and provide the exact command for manual execution.

## Assignment deliverables

Maintain:
- `intent.md`
- `Final directive.md`
- `README.md`
- `specs/`
- `tasks/`
- `logs/day-1.md` through `logs/day-5.md`
- `reflection/what-i-unlearned.md`

Document actual AI usage, verification, failures, corrections, and changes in working habits.

Do not fabricate activities, failures, test results, or lessons.

## Start

Inspect the repository and prepare the specifications and dependency-ordered task plan.

Do not implement application features until the user approves the first task.