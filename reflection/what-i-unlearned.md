# What I Unlearned

## Non-Obvious Insights from Building Stall Alerter

### 1. AGP 9.5.0 / Kotlin 2.2.10 Compose Compiler Plugin Trap
**The Problem:** When using AGP 9.5.0, which bundles Kotlin 2.2.10, the Compose compiler plugin was not being applied automatically. The `kotlinCompilerExtensionVersion` in `composeOptions` was being ignored/overridden by AGP's bundled plugin.

**The Fix:** Explicitly apply the Compose compiler plugin matching the bundled Kotlin version:
```kotlin
plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10"
}
```

**Why It Matters:** Without this explicit plugin, Compose IR compilation fails with the misleading error "couldn't find inline method Landroidx/compose/foundation/layout/ColumnKt;.Column(...)V". The error suggests a dependency issue, but it's actually a compiler plugin version mismatch. This was the single biggest blocker for Day 3.

**Lesson:** Always verify the Compose compiler plugin is explicitly applied when using AGP that bundles its own Kotlin version. Check `./gradlew :app:compileDebugKotlin --debug` for "Kotlin compiler classpath" to verify the plugin is loaded.

---

### 2. Navigation Compose Version Alignment
**The Problem:** Adding `androidx.navigation:navigation-compose` to the project introduced version conflicts with AGP 9.5.0 / Kotlin 2.2.10. The navigation graph XML generation failed with errors like "attribute startDestination not found" and "attribute android:navUpCommand not found".

**The Workaround:** Hosting Compose screens directly without the Navigation Compose graph, using explicit `navController.navigate()` calls where needed. This avoids the library version conflict while preserving screen-to-screen navigation.

**Lesson:** New library versions may not align with the project's AGP/Kotlin version. When version conflicts arise, evaluate whether the feature justifies the upgrade complexity, or use direct navigation as a pragmatic alternative.

---

### 3. Material 3 theming requires both XML and Compose definitions
**The Problem:** The base theme is defined in `values/themes.xml` as `Theme.MaterialComponents.DayNight.DarkActionBar`, but Material 3 Compose UI requires additional `ColorScheme`, `Typography`, and `Shapes` definitions in Compose `Theme.kt` format. These two theming systems need to be consistent but serve different purposes.

**The Discovery:** XML theme defines activities and base colors; Compose theme provides the `MaterialTheme` composable that UI components read via `currentMaterialTheme()`. Both must be kept in sync for visual consistency, but they operate at different levels of the Android stack.

**Lesson:** When adopting Material 3, plan for both XML theme declarations and Compose theme configuration. They're complementary, not redundant.

---

### 4. CSV parsing edge cases are harder than expected
**The Problem:** The CSV import parser (`CsvImportRepo.parseCsv()`) had to handle numerous edge cases that weren't apparent from the spec:
- UTF-8 BOM (`﻿`) at the start of files
- CRLF vs LF line endings
- Extra whitespace in header names and values
- Invalid date formats that skip entire rows
- Unknown stage values that need graceful handling
- Empty names that should be filtered out

**The Discovery:** The `parseCsv()` method required 30+ lines of defensive code just to handle encoding, formatting, and validation edge cases. Each rule from `specs/csv-schema.md` required implementation details not obvious from the high-level specification.

**Lesson:** Specifications capture the "what" but the "how" often reveals more edge cases than anticipated. Build parsers with defensive coding from the start — it's easier to add validation upfront than to retrofit error handling later.

---

### 5. Pure Kotlin business logic is trivially testable — but only when dependencies are injected
**The Problem:** Days 1-3 demonstrated that PriorityCalculator, FlagEngine, and NextActionRecommender are straightforward to test when they receive dependencies via constructor injection (Clock, CsvRepository). However, the ViewModel's `onCandidatesParsed()` method combines business logic with UI-state transformation (sorting, tier mapping), making it harder to test in isolation.

**The Discovery:** The 33 unit tests all pass because the business functions are pure Kotlin with no Android dependencies. However, the ViewModel layer requires instrumentation tests or careful unit test setup to mock the `ContentResolver` for CSV import paths.

**Lesson:** Maximize pure Kotlin business functions (zero Android SDK calls) for fast, deterministic unit tests. Keep Android-dependent code minimal and well-abstracted behind repositories/interfaces.

---

## Summary of Working Habits Changed

Due to the AI-first, specification-driven workflow:

- **Specification-first development:** Reading `specs/` files before writing code prevented rework and ensured business rules were correct from the start.
- **Explicit dependency management:** The AGP/Kotlin/Compose version alignment became a first-class concern, not an afterthought.
- **Documentation as we go:** Maintaining `logs/day-1.md` through `day-3.md` (and now `day-4.md`) created a useful audit trail and reduced context-switching cost.
- **Test-driven verification:** Running tests after each implementation step (not just at the end) caught issues early and provided confidence that changes didn't break existing functionality.
- **Embracing workarounds:** When the Navigation Compose library conflicted with AGP 9.5.0, using direct navigation was a pragmatic alternative to upgrading the entire stack.

---

**Non-obvious count:** 5 insights captured. These will form the basis of `reflection/what-i-unlearned.md` on Day 5.