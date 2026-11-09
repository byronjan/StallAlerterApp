package com.example.stallalerterapp.business.flags

import com.example.stallalerterapp.data.csv.Candidate
import com.example.stallalerterapp.data.csv.Stage
import com.example.stallalerterapp.model.Flag
import com.example.stallalerterapp.model.FlagId

/**
 * Computes all applicable flags for a candidate based on their stage and days since last contact.
 *
 * Rules per specs/flag-criteria.md:
 * - Multiple flags can apply simultaneously (cumulative adjustments)
 * - RECENT_ACTIVITY (days < 7) overrides all stale/stuck/offer flags
 * - HIRED_STALE takes precedence for hired candidates (replaces all others, tier = None)
 * - OFFER_STALE only applies when stage == offer
 * - Display order: HIRED_STALE > STALE_60 > STALE_30 > STALE_14 > STUCK_SCREENING > STUCK_TECH > OFFER_STALE > RECENT_ACTIVITY
 */
object FlagEngine {

    /** Compute all flags for a candidate. */
    fun computeFlags(candidate: Candidate, daysSinceLastContact: Int): List<Flag> {
        val stage = candidate.stage

        // HIRED_STALE takes absolute precedence
        if (stage == Stage.hired) {
            return if (daysSinceLastContact >= 30) {
                listOf(Flag(
                    flagId = FlagId.HIRED_STALE,
                    description = "Hired candidate — no action needed",
                    scoreAdjustment = 0 // tier forced to None regardless
                ))
            } else {
                emptyList()
            }
        }

        // RECENT_ACTIVITY overrides all stale/stuck/offer flags
        if (daysSinceLastContact < 7) {
            return listOf(Flag(
                flagId = FlagId.RECENT_ACTIVITY,
                description = "Recent activity — no follow-up needed",
                scoreAdjustment = -5
            ))
        }

        val flags = mutableListOf<Flag>()

        // STALE flags (cumulative, all shown)
        if (daysSinceLastContact >= 60) {
            flags.add(Flag(FlagId.STALE_60, "No contact in 60+ days", 15))
        }
        if (daysSinceLastContact >= 30) {
            flags.add(Flag(FlagId.STALE_30, "No contact in 30+ days", 10))
        }
        if (daysSinceLastContact >= 14) {
            flags.add(Flag(FlagId.STALE_14, "No contact in 14+ days", 5))
        }

        // STUCK flags
        if (stage == Stage.screening && daysSinceLastContact >= 21) {
            flags.add(Flag(FlagId.STUCK_SCREENING, "Stuck in screening for 21+ days", 8))
        }
        if (stage == Stage.technicalInterview && daysSinceLastContact >= 35) {
            flags.add(Flag(FlagId.STUCK_TECH, "Stuck in technical interview for 35+ days", 12))
        }

        // OFFER_STALE only for offer stage
        if (stage == Stage.offer && daysSinceLastContact >= 14) {
            flags.add(Flag(FlagId.OFFER_STALE, "Offer pending for 14+ days — escalate", 20))
        }

        // Return in spec display order: HIRED_STALE handled above, then STALE_60, STALE_30, STALE_14, STUCK_*, OFFER_STALE, RECENT_ACTIVITY handled above
        // Our list is already built in the correct order
        return flags
    }
}