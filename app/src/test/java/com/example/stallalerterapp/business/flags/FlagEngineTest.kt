package com.example.stallalerterapp.business.flags

import com.example.stallalerterapp.data.csv.Candidate
import com.example.stallalerterapp.data.csv.Stage
import com.example.stallalerterapp.model.Flag
import com.example.stallalerterapp.model.FlagId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FlagEngineTest {

    private fun candidate(
        name: String,
        stage: Stage,
        lastContact: LocalDate
    ): Candidate = Candidate(name, stage, lastContact)

    @Test
    fun computeFlags_recentActivity_whenDaysLessThan7_returnsOnlyRecentActivityFlag() {
        val c = candidate("Alice", Stage.screening, LocalDate.of(2026, 10, 30)) // 5 days ago
        val flags = FlagEngine.computeFlags(c, 5)

        assertEquals(1, flags.size)
        assertEquals(FlagId.RECENT_ACTIVITY, flags[0].flagId)
        assertEquals("Recent activity — no follow-up needed", flags[0].description)
        assertEquals(-5, flags[0].scoreAdjustment)
    }

    @Test
    fun computeFlags_hiredStale_whenHiredAndDaysGe30_returnsOnlyHiredStaleFlag() {
        val c = candidate("Bob", Stage.hired, LocalDate.of(2026, 9, 1)) // 64 days ago
        val flags = FlagEngine.computeFlags(c, 64)

        assertEquals(1, flags.size)
        assertEquals(FlagId.HIRED_STALE, flags[0].flagId)
        assertEquals("Hired candidate — no action needed", flags[0].description)
    }

    @Test
    fun computeFlags_hiredStale_whenHiredAndDaysLt30_returnsEmptyList() {
        val c = candidate("Bob", Stage.hired, LocalDate.of(2026, 10, 20)) // 15 days ago
        val flags = FlagEngine.computeFlags(c, 15)

        assertEquals(0, flags.size)
    }

    @Test
    fun computeFlags_stale14_whenDaysGe14_returnsStale14Flag() {
        val c = candidate("Charlie", Stage.screening, LocalDate.of(2026, 10, 1)) // 34 days ago
        val flags = FlagEngine.computeFlags(c, 34)

        val stale14 = flags.find { it.flagId == FlagId.STALE_14 }
        assertTrue("STALE_14 should be present", stale14 != null)
        assertEquals("No contact in 14+ days", stale14!!.description)
        assertEquals(5, stale14.scoreAdjustment)
    }

    @Test
    fun computeFlags_stale30_whenDaysGe30_returnsStale30Flag() {
        val c = candidate("Charlie", Stage.screening, LocalDate.of(2026, 10, 1)) // 34 days ago
        val flags = FlagEngine.computeFlags(c, 34)

        val stale30 = flags.find { it.flagId == FlagId.STALE_30 }
        assertTrue("STALE_30 should be present", stale30 != null)
        assertEquals("No contact in 30+ days", stale30!!.description)
        assertEquals(10, stale30.scoreAdjustment)
    }

    @Test
    fun computeFlags_stale60_whenDaysGe60_returnsStale60Flag() {
        val c = candidate("Charlie", Stage.screening, LocalDate.of(2026, 9, 1)) // 64 days ago
        val flags = FlagEngine.computeFlags(c, 64)

        val stale60 = flags.find { it.flagId == FlagId.STALE_60 }
        assertTrue("STALE_60 should be present", stale60 != null)
        assertEquals("No contact in 60+ days", stale60!!.description)
        assertEquals(15, stale60.scoreAdjustment)
    }

    @Test
    fun computeFlags_staleFlags_whenDaysGe60_returnsAllThreeStaleFlagsCumulative() {
        val c = candidate("Charlie", Stage.screening, LocalDate.of(2026, 9, 1)) // 64 days ago
        val flags = FlagEngine.computeFlags(c, 64)

        val staleFlags = flags.filter { it.flagId in listOf(FlagId.STALE_14, FlagId.STALE_30, FlagId.STALE_60) }
        assertEquals(3, staleFlags.size)

        val totalAdjustment = staleFlags.sumOf { it.scoreAdjustment }
        assertEquals(30, totalAdjustment)
    }

    @Test
    fun computeFlags_stuckScreening_whenScreeningAndDaysGe21_returnsStuckScreeningFlag() {
        val c = candidate("Diana", Stage.screening, LocalDate.of(2026, 10, 1)) // 34 days ago
        val flags = FlagEngine.computeFlags(c, 34)

        val stuck = flags.find { it.flagId == FlagId.STUCK_SCREENING }
        assertTrue("STUCK_SCREENING should be present", stuck != null)
        assertEquals("Stuck in screening for 21+ days", stuck!!.description)
        assertEquals(8, stuck.scoreAdjustment)
    }

    @Test
    fun computeFlags_stuckTech_whenTechnicalInterviewAndDaysGe35_returnsStuckTechFlag() {
        val c = candidate("Evan", Stage.technicalInterview, LocalDate.of(2026, 9, 20)) // 45 days ago
        val flags = FlagEngine.computeFlags(c, 45)

        val stuck = flags.find { it.flagId == FlagId.STUCK_TECH }
        assertTrue("STUCK_TECH should be present", stuck != null)
        assertEquals("Stuck in technical interview for 35+ days", stuck!!.description)
        assertEquals(12, stuck.scoreAdjustment)
    }

    @Test
    fun computeFlags_offerStale_whenOfferAndDaysGe14_returnsOfferStaleFlag() {
        val c = candidate("Fiona", Stage.offer, LocalDate.of(2026, 10, 1)) // 34 days ago
        val flags = FlagEngine.computeFlags(c, 34)

        val offerStale = flags.find { it.flagId == FlagId.OFFER_STALE }
        assertTrue("OFFER_STALE should be present", offerStale != null)
        assertEquals("Offer pending for 14+ days — escalate", offerStale!!.description)
        assertEquals(20, offerStale.scoreAdjustment)
    }

    @Test
    fun computeFlags_offerStale_whenNotOfferStage_returnsNoOfferStaleFlag() {
        val c = candidate("Fiona", Stage.screening, LocalDate.of(2026, 10, 1)) // 34 days ago
        val flags = FlagEngine.computeFlags(c, 34)

        val offerStale = flags.find { it.flagId == FlagId.OFFER_STALE }
        assertFalse("OFFER_STALE should NOT be present for screening", offerStale != null)
    }

    @Test
    fun computeFlags_returnsFlagsInSpecDisplayOrder() {
        val c = candidate("Charlie", Stage.screening, LocalDate.of(2026, 9, 1)) // 64 days ago
        val flags = FlagEngine.computeFlags(c, 64)

        val flagIds = flags.map { it.flagId }
        assertEquals(
            listOf(FlagId.STALE_60, FlagId.STALE_30, FlagId.STALE_14, FlagId.STUCK_SCREENING),
            flagIds
        )
    }

    @Test
    fun computeFlags_specExample_screening644Days_returnsExpectedFlags() {
        val c = candidate("Test", Stage.screening, LocalDate.of(2025, 1, 5)) // ~644 days before 2026-11-04
        val flags = FlagEngine.computeFlags(c, 644)

        val flagIds = flags.map { it.flagId }.toSet()
        assertTrue("Should have STALE_60", flagIds.contains(FlagId.STALE_60))
        assertTrue("Should have STALE_30", flagIds.contains(FlagId.STALE_30))
        assertTrue("Should have STALE_14", flagIds.contains(FlagId.STALE_14))
        assertTrue("Should have STUCK_SCREENING", flagIds.contains(FlagId.STUCK_SCREENING))

        val totalAdjustment = flags.sumOf { it.scoreAdjustment }
        assertEquals(38, totalAdjustment)
    }
}