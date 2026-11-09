package com.example.stallalerterapp.model

/**
 * Discrete explanations of why a candidate is not progressing.
 * Defined by specs/flag-criteria.md.
 */
enum class FlagId {
    STALE_14,
    STALE_30,
    STALE_60,
    STUCK_SCREENING,
    STUCK_TECH,
    RECENT_ACTIVITY,
    OFFER_STALE,
    HIRED_STALE
}

/**
 * A single flag computed for a candidate.
 *
 * @param flagId        the kind of flag (see [FlagId])
 * @param description   human-readable explanation shown in the UI
 * @param scoreAdjustment how much this flag changes the priority score
 */
data class Flag(
    val flagId: FlagId,
    val description: String,
    val scoreAdjustment: Int
)