package com.example.stallalerterapp.data.csv

import java.time.LocalDate
import kotlin.jvm.JvmName

/** Represents a hiring stage for a candidate. */
enum class Stage {
    screening,
    technicalInterview,
    onSite,
    offer,
    rejected,
    hired

    /** Convert from CSV string value to Stage enum. */
    companion object {
        @JvmName("fromCsvString")
        fun fromCsvString(value: String?): Stage? {
            return when (value?.lowercase()) {
                "screening" -> screening
                "technical-interview" -> technicalInterview
                "on-site" -> onSite
                "offer" -> offer
                "rejected" -> rejected
                "hired" -> hired
                else -> null
            }
        }

        /** Get all valid stage values as CSV column headers. */
        fun getColumnHeaders(): List<String> = listOf(
            "screening",
            "technical-interview",
            "on-site",
            "offer",
            "rejected",
            "hired"
        )
    }
}

/** Represents a single candidate from the hiring-board CSV import. */
data class Candidate(
    val fullName: String,
    val stage: Stage,
    val lastContact: LocalDate,
    val email: String? = null,
    val phone: String? = null,
    val dateAdded: LocalDate? = null,
    val notes: String? = null,
    val recruiter: String? = null
) {
    /** Valid stage values for CSV import. */
    companion object {
        val VALID_STAGES = Stage.getColumnHeaders()
    }
}