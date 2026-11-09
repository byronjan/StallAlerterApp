package com.example.stallalerterapp.business

import com.example.stallalerterapp.data.csv.Candidate
import com.example.stallalerterapp.data.csv.Stage
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Computes the deterministic priority score for a candidate.
 *
 * Formula (specs/priority-model.md):
 *   score = days_since_last_contact + stage_weight
 *
 * Uses an injected [Clock] so results are reproducible in tests.
 */
object PriorityCalculator {

    /** Fixed per-stage offset applied to the base score. */
    private fun stageWeight(stage: Stage): Int = when (stage) {
        Stage.screening -> 0
        Stage.technicalInterview -> 10
        Stage.onSite -> 20
        Stage.offer -> 30
        Stage.rejected -> -10
        Stage.hired -> -20
    }

    /**
     * @param candidate the candidate to score
     * @param clock     injected clock used for "today"
     * @return priority score (higher = more urgent)
     */
    fun computeScore(candidate: Candidate, clock: Clock): Int {
        val today = LocalDate.now(clock)
        val daysSinceLastContact = ChronoUnit.DAYS.between(candidate.lastContact, today).toInt()
        return daysSinceLastContact + stageWeight(candidate.stage)
    }
}