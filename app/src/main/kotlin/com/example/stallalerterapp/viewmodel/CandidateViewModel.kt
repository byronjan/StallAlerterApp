package com.example.stallalerterapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.stallalerterapp.business.PriorityCalculator
import com.example.stallalerterapp.business.actions.NextActionRecommender
import com.example.stallalerterapp.business.flags.FlagEngine
import com.example.stallalerterapp.data.csv.Candidate
import com.example.stallalerterapp.data.csv.Stage
import com.example.stallalerterapp.model.NextAction
import com.example.stallalerterapp.model.PriorityTier
import com.example.stallalerterapp.model.PriorityTierMapper
import com.example.stallalerterapp.repo.CsvImportRepo
import com.example.stallalerterapp.repo.CsvRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * ViewModel for the candidate list screen.
 *
 * Exposes [uiState] with the full computed list:
 * candidate + flags + base score + adjusted score + priority tier.
 *
 * All business logic is pure Kotlin; the ViewModel only coordinates.
 */
class CandidateViewModel(
    private val csvRepository: CsvRepository,
    private val clock: Clock = Clock.systemUTC(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow<CandidateListUiState>(CandidateListUiState.Empty())
    val uiState = _uiState.asStateFlow()

    /** Detail state for the selected candidate. */
    private val _detailState = MutableStateFlow<CandidateUiState?>(null)
    val detailState = _detailState.asStateFlow()

    /** Recommended next action for the selected candidate. */
    private val _nextAction = MutableStateFlow<NextAction?>(null)
    val nextAction = _nextAction.asStateFlow()

    /** Select a candidate to view details and compute recommended action. */
    fun selectCandidate(candidateUiState: CandidateUiState) {
        _detailState.value = candidateUiState
        _nextAction.value = NextActionRecommender.recommendNextAction(
            candidateUiState.candidate,
            candidateUiState.flags
        )
    }

    /** Clear the selected candidate detail. */
    fun clearSelection() {
        _detailState.value = null
        _nextAction.value = null
    }

    /**
     * Import a CSV selected through the Storage Access Framework.
     *
     * Reading + parsing runs on [ioDispatcher]; results are delivered
     * via [uiState] as Loading -> Success / Error / Empty / NoData.
     */
    fun importCsv(uri: Uri) {
        _uiState.value = CandidateListUiState.Loading
        viewModelScope.launch {
            try {
                val result = withContext(ioDispatcher) {
                    csvRepository.importCsv(uri)
                }
                result.onSuccess { candidates ->
                    if (candidates.isEmpty()) {
                        _uiState.value = CandidateListUiState.NoData()
                    } else {
                        onCandidatesParsed(candidates)
                    }
                }.onFailure { error ->
                    _uiState.value = CandidateListUiState.Error(
                        error.message ?: "Unknown error during import"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = CandidateListUiState.Error(
                    e.message ?: "Could not read the selected file"
                )
            }
        }
    }

    /** Called with already-parsed candidates (for testability and separation of concerns). */
    fun onCandidatesParsed(candidates: List<Candidate>) {
        val today = LocalDate.now(clock)

        val uiStates = candidates.map { candidate ->
            val daysSince = ChronoUnit.DAYS.between(candidate.lastContact, today).toInt()
            val baseScore = PriorityCalculator.computeScore(candidate, clock)
            val flags = FlagEngine.computeFlags(candidate, daysSince)
            val flagAdjustment = flags.sumOf { it.scoreAdjustment }
            val adjustedScore = baseScore + flagAdjustment
            val tier = if (candidate.stage == Stage.hired &&
                         daysSince >= 30) {
                PriorityTier.None
            } else {
                PriorityTierMapper.fromScore(adjustedScore)
            }
            val nextAction = NextActionRecommender.recommendNextAction(candidate, flags)
            CandidateUiState(
                candidate = candidate,
                flags = flags,
                baseScore = baseScore,
                adjustedScore = adjustedScore,
                tier = tier,
                nextAction = nextAction
            )
        }

        // Stable ordering: score desc, then lastContact asc (oldest first), then fullName asc
        val sorted = uiStates.sortedWith(
            compareByDescending<CandidateUiState> { it.adjustedScore }
                .thenBy { it.candidate.lastContact }
                .thenBy { it.candidate.fullName }
        )

        _uiState.value = if (sorted.isEmpty()) {
            CandidateListUiState.Empty()
        } else {
            CandidateListUiState.Success(sorted)
        }
    }

    companion object {
        /**
         * Default factory: builds the CSV repository from the Application's
         * ContentResolver (SAF access), everything else uses defaults.
         */
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(
                    extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                )
                return CandidateViewModel(
                    csvRepository = CsvImportRepo(application.contentResolver)
                ) as T
            }
        }
    }
}