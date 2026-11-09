package com.example.stallalerterapp.business.actions

import com.example.stallalerterapp.data.csv.Candidate
import com.example.stallalerterapp.data.csv.Stage
import com.example.stallalerterapp.model.Flag
import com.example.stallalerterapp.model.FlagId
import com.example.stallalerterapp.model.NextAction
import com.example.stallalerterapp.model.ActionId

/**
 * Recommends the next action for a candidate based on stage and flags.
 * Pure Kotlin function — no dependencies, fully testable.
 */
object NextActionRecommender {

    /**
     * Returns the recommended next action for a candidate.
     * @param candidate The candidate data
     * @param flags Computed flags from FlagEngine
     * @return NextAction with title, optional subtitle, and actionId
     */
    fun recommendNextAction(candidate: Candidate, flags: List<Flag>): NextAction {
        // HIRED_STALE takes absolute precedence
        if (flags.any { it.flagId == FlagId.HIRED_STALE }) {
            return NextAction(
                title = "No action needed",
                subtitle = "Candidate has been hired",
                actionId = ActionId.NO_ACTION_NEEDED
            )
        }

        // OFFER_STALE overrides stage-based action
        if (flags.any { it.flagId == FlagId.OFFER_STALE }) {
            return NextAction(
                title = "Escalate to hiring manager",
                subtitle = "Offer pending for 14+ days",
                actionId = ActionId.ESCALATE_TO_HIRING_MANAGER
            )
        }

        // Stage-based default actions
        return when (candidate.stage) {
            Stage.screening -> NextAction(
                title = "Send follow-up email",
                subtitle = "Candidate in screening",
                actionId = ActionId.SEND_FOLLOWUP_EMAIL
            )
            Stage.technicalInterview -> NextAction(
                title = "Send technical assignment",
                subtitle = "Candidate in technical interview",
                actionId = ActionId.SEND_TECHNICAL_ASSIGNMENT
            )
            Stage.onSite -> NextAction(
                title = "Send thank-you note",
                subtitle = "Candidate completed on-site",
                actionId = ActionId.SEND_THANK_YOU_NOTE
            )
            Stage.offer -> NextAction(
                title = "Extend offer deadline",
                subtitle = "Candidate has active offer",
                actionId = ActionId.EXTEND_OFFER_DEADLINE
            )
            Stage.rejected, Stage.hired -> NextAction(
                title = "No action needed",
                subtitle = "Candidate is ${candidate.stage.name.lowercase()}",
                actionId = ActionId.NO_ACTION_NEEDED
            )
        }
    }
}