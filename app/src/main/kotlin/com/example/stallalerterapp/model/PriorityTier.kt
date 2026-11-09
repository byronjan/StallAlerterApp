package com.example.stallalerterapp.model

/**
 * Priority tiers derived from the final priority score.
 * Defined by specs/priority-model.md.
 */
enum class PriorityTier {
    Critical,
    High,
    Medium,
    Low,
    None
}

/** Maps a priority score to its tier. */
object PriorityTierMapper {
    fun fromScore(score: Int): PriorityTier = when (score) {
        in Int.MIN_VALUE..9 -> if (score < 0) PriorityTier.None else PriorityTier.Low
        in 10..29 -> PriorityTier.Medium
        in 30..59 -> PriorityTier.High
        else -> PriorityTier.Critical // 60+
    }
}