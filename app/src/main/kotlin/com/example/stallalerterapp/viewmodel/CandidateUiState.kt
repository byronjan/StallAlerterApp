package com.example.stallalerterapp.viewmodel

import com.example.stallalerterapp.data.csv.Candidate
import com.example.stallalerterapp.model.Flag
import com.example.stallalerterapp.model.NextAction
import com.example.stallalerterapp.model.PriorityTier

/**
 * UI state for a single candidate with computed flags, score, tier, and next action.
 */
data class CandidateUiState(
    val candidate: Candidate,
    val flags: List<Flag>,
    val baseScore: Int,
    val adjustedScore: Int,
    val tier: PriorityTier,
    val nextAction: NextAction?
) {
    /** Sum of flag score adjustments. */
    val flagAdjustment: Int = flags.sumOf { it.scoreAdjustment }
}

/** Combined UI state for the candidate list screen. */
sealed interface CandidateListUiState {
    object Loading : CandidateListUiState
    data class Success(val candidates: List<CandidateUiState>) : CandidateListUiState
    data class Error(val message: String) : CandidateListUiState
    /** Shown before any import attempt: user hasn't picked a file yet */
    data class Empty(val message: String = "No candidates imported yet.\nTap the import button above to select a CSV file.") : CandidateListUiState
    /** Shown after a valid-file import that produced zero parseable candidates */
    data class NoData(val message: String = "No candidate data found in CSV") : CandidateListUiState
}