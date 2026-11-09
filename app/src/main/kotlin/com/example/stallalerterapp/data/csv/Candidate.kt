package com.example.stallalerterapp.data.csv

import java.time.LocalDate

/** Represents a hiring stage for a candidate. */
enum class Stage {
    screening,
    technicalInterview,
    onSite,
    offer,
    rejected,
    hired
}

/** Represents a single candidate from the hiring-board CSV import. */
data class Candidate(
    val fullName: String,
    val stage: Stage,
    val lastContact: LocalDate,
    val email: String? = null,
    val phone: String? = null
)
