package com.example.stallalerterapp.business.actions

import com.example.stallalerterapp.data.csv.Candidate
import com.example.stallalerterapp.data.csv.Stage
import com.example.stallalerterapp.model.Flag
import com.example.stallalerterapp.model.FlagId
import com.example.stallalerterapp.model.NextAction
import com.example.stallalerterapp.model.ActionId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate

class NextActionRecommenderTest {

    private fun candidate(
        name: String,
        stage: Stage,
        lastContact: LocalDate
    ): Candidate = Candidate(name, stage, lastContact)

    private fun flag(id: FlagId): Flag = Flag(id, "description", 0)

    @Test
    fun recommendNextAction_screening_returnsSendFollowupEmail() {
        val c = candidate("Alice", Stage.screening, LocalDate.of(2026, 10, 1))
        val action = NextActionRecommender.recommendNextAction(c, emptyList())

        assertEquals(ActionId.SEND_FOLLOWUP_EMAIL, action.actionId)
        assertEquals("Send follow-up email", action.title)
        assertEquals("Candidate in screening", action.subtitle)
    }

    @Test
    fun recommendNextAction_technicalInterview_returnsSendTechnicalAssignment() {
        val c = candidate("Bob", Stage.technicalInterview, LocalDate.of(2026, 10, 1))
        val action = NextActionRecommender.recommendNextAction(c, emptyList())

        assertEquals(ActionId.SEND_TECHNICAL_ASSIGNMENT, action.actionId)
        assertEquals("Send technical assignment", action.title)
        assertEquals("Candidate in technical interview", action.subtitle)
    }

    @Test
    fun recommendNextAction_onSite_returnsSendThankYouNote() {
        val c = candidate("Charlie", Stage.onSite, LocalDate.of(2026, 10, 1))
        val action = NextActionRecommender.recommendNextAction(c, emptyList())

        assertEquals(ActionId.SEND_THANK_YOU_NOTE, action.actionId)
        assertEquals("Send thank-you note", action.title)
        assertEquals("Candidate completed on-site", action.subtitle)
    }

    @Test
    fun recommendNextAction_offer_returnsExtendOfferDeadline() {
        val c = candidate("Diana", Stage.offer, LocalDate.of(2026, 10, 1))
        val action = NextActionRecommender.recommendNextAction(c, emptyList())

        assertEquals(ActionId.EXTEND_OFFER_DEADLINE, action.actionId)
        assertEquals("Extend offer deadline", action.title)
        assertEquals("Candidate has active offer", action.subtitle)
    }

    @Test
    fun recommendNextAction_rejected_returnsNoActionNeeded() {
        val c = candidate("Evan", Stage.rejected, LocalDate.of(2026, 10, 1))
        val action = NextActionRecommender.recommendNextAction(c, emptyList())

        assertEquals(ActionId.NO_ACTION_NEEDED, action.actionId)
        assertEquals("No action needed", action.title)
        assertEquals("Candidate is rejected", action.subtitle)
    }

    @Test
    fun recommendNextAction_hiredWithoutFlag_returnsNoActionNeeded() {
        val c = candidate("Fiona", Stage.hired, LocalDate.of(2026, 10, 1))
        val action = NextActionRecommender.recommendNextAction(c, emptyList())

        assertEquals(ActionId.NO_ACTION_NEEDED, action.actionId)
        assertEquals("No action needed", action.title)
        assertEquals("Candidate is hired", action.subtitle)
    }

    @Test
    fun recommendNextAction_hiredStaleFlag_overridesStageReturnsNoActionNeeded() {
        val c = candidate("George", Stage.screening, LocalDate.of(2026, 10, 1))
        val action = NextActionRecommender.recommendNextAction(c, listOf(flag(FlagId.HIRED_STALE)))

        assertEquals(ActionId.NO_ACTION_NEEDED, action.actionId)
        assertEquals("No action needed", action.title)
        assertEquals("Candidate has been hired", action.subtitle)
    }

    @Test
    fun recommendNextAction_offerStaleFlag_onOfferStage_returnsEscalate() {
        val c = candidate("Helen", Stage.offer, LocalDate.of(2026, 9, 15)) // 50 days ago
        val action = NextActionRecommender.recommendNextAction(c, listOf(flag(FlagId.OFFER_STALE)))

        assertEquals(ActionId.ESCALATE_TO_HIRING_MANAGER, action.actionId)
        assertEquals("Escalate to hiring manager", action.title)
        assertEquals("Offer pending for 14+ days", action.subtitle)
    }

    @Test
    fun recommendNextAction_offerStaleFlag_onOtherStage_overridesStageReturnsEscalate() {
        val c = candidate("Ian", Stage.screening, LocalDate.of(2026, 9, 15))
        val action = NextActionRecommender.recommendNextAction(c, listOf(flag(FlagId.OFFER_STALE)))

        assertEquals(ActionId.ESCALATE_TO_HIRING_MANAGER, action.actionId)
        assertEquals("Escalate to hiring manager", action.title)
        assertEquals("Offer pending for 14+ days", action.subtitle)
    }

    @Test
    fun recommendNextAction_multipleFlagsIncludingHiredStale_returnsNoActionNeeded() {
        val c = candidate("Jack", Stage.offer, LocalDate.of(2026, 9, 15))
        val action = NextActionRecommender.recommendNextAction(c, listOf(
            flag(FlagId.OFFER_STALE),
            flag(FlagId.HIRED_STALE),
            flag(FlagId.STALE_30)
        ))

        assertEquals(ActionId.NO_ACTION_NEEDED, action.actionId)
        assertEquals("No action needed", action.title)
    }

    @Test
    fun recommendNextAction_multipleFlagsIncludingOfferStaleButNotHiredStale_returnsEscalate() {
        val c = candidate("Kate", Stage.screening, LocalDate.of(2026, 9, 15))
        val action = NextActionRecommender.recommendNextAction(c, listOf(
            flag(FlagId.OFFER_STALE),
            flag(FlagId.STALE_30)
        ))

        assertEquals(ActionId.ESCALATE_TO_HIRING_MANAGER, action.actionId)
        assertEquals("Escalate to hiring manager", action.title)
    }

    @Test
    fun recommendNextAction_isDeterministic() {
        val c = candidate("Liam", Stage.technicalInterview, LocalDate.of(2026, 10, 1))
        val flags = listOf(flag(FlagId.STALE_14))
        val action1 = NextActionRecommender.recommendNextAction(c, flags)
        val action2 = NextActionRecommender.recommendNextAction(c, flags)

        assertEquals(action1.actionId, action2.actionId)
        assertEquals(action1.title, action2.title)
        assertEquals(action1.subtitle, action2.subtitle)
    }
}