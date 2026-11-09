package com.example.stallalerterapp.business

import com.example.stallalerterapp.data.csv.Candidate
import com.example.stallalerterapp.data.csv.Stage
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

class PriorityCalculatorTest {

    /** Fixed clock: 2026-11-04 in system default zone (matches LocalDate.now behavior). */
    private val fixedClock = Clock.fixed(
        LocalDate.of(2026, 11, 4).atStartOfDay(ZoneId.systemDefault()).toInstant(),
        ZoneId.systemDefault()
    )

    private fun candidate(
        name: String,
        stage: Stage,
        lastContact: LocalDate
    ): Candidate = Candidate(name, stage, lastContact)

    @Test
    fun computeScore_screening_returnsDaysSincePlusZeroWeight() {
        val c = candidate("Alice", Stage.screening, LocalDate.of(2025, 1, 1))
        // 2025-01-01 to 2026-11-04 = 672 days
        assertEquals(672 + 0, PriorityCalculator.computeScore(c, fixedClock))
    }

    @Test
    fun computeScore_technicalInterview_returnsDaysSincePlusTenWeight() {
        val c = candidate("Bob", Stage.technicalInterview, LocalDate.of(2025, 2, 15))
        // 2025-02-15 to 2026-11-04 = 627 days
        assertEquals(627 + 10, PriorityCalculator.computeScore(c, fixedClock))
    }

    @Test
    fun computeScore_onSite_returnsDaysSincePlusTwentyWeight() {
        val c = candidate("Charlie", Stage.onSite, LocalDate.of(2025, 3, 1))
        // 2025-03-01 to 2026-11-04 = 613 days
        assertEquals(613 + 20, PriorityCalculator.computeScore(c, fixedClock))
    }

    @Test
    fun computeScore_offer_returnsDaysSincePlusThirtyWeight() {
        val c = candidate("Diana", Stage.offer, LocalDate.of(2025, 1, 20))
        // 2025-01-20 to 2026-11-04 = 653 days
        assertEquals(653 + 30, PriorityCalculator.computeScore(c, fixedClock))
    }

    @Test
    fun computeScore_rejected_returnsDaysSinceMinusTenWeight() {
        val c = candidate("Evan", Stage.rejected, LocalDate.of(2025, 5, 1))
        // 2025-05-01 to 2026-11-04 = 552 days, -10 weight
        assertEquals(552 - 10, PriorityCalculator.computeScore(c, fixedClock))
    }

    @Test
    fun computeScore_hired_returnsDaysSinceMinusTwentyWeight() {
        val c = candidate("Fiona", Stage.hired, LocalDate.of(2025, 6, 1))
        // 2025-06-01 to 2026-11-04 = 521 days, -20 weight
        assertEquals(521 - 20, PriorityCalculator.computeScore(c, fixedClock))
    }

    @Test
    fun computeScore_isDeterministicWithFixedClock() {
        val c = candidate("Test", Stage.screening, LocalDate.of(2025, 1, 1))
        val score1 = PriorityCalculator.computeScore(c, fixedClock)
        val score2 = PriorityCalculator.computeScore(c, fixedClock)
        assertEquals(score1, score2)
    }
}