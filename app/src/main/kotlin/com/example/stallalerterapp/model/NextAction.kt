package com.example.stallalerterapp.model

/** Enum representing the type of recommended action. */
enum class ActionId {
    SEND_FOLLOWUP_EMAIL,
    SCHEDULE_PHONE_SCREEN,
    SEND_TECHNICAL_ASSIGNMENT,
    SCHEDULE_FOLLOWUP_INTERVIEW,
    SEND_THANK_YOU_NOTE,
    SCHEDULE_ONSITE_FOLLOWUP,
    ESCALATE_TO_HIRING_MANAGER,
    EXTEND_OFFER_DEADLINE,
    NO_ACTION_NEEDED
}

/** Represents a recommended next action for a candidate. */
data class NextAction(
    val title: String,
    val subtitle: String? = null,
    val actionId: ActionId
)