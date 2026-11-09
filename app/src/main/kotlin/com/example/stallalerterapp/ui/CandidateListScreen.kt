package com.example.stallalerterapp.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.stallalerterapp.data.csv.Candidate
import com.example.stallalerterapp.data.csv.Stage
import com.example.stallalerterapp.model.ActionId
import com.example.stallalerterapp.model.Flag
import com.example.stallalerterapp.model.FlagId
import com.example.stallalerterapp.model.NextAction
import com.example.stallalerterapp.model.PriorityTier
import com.example.stallalerterapp.viewmodel.CandidateViewModel
import com.example.stallalerterapp.viewmodel.CandidateUiState
import com.example.stallalerterapp.viewmodel.CandidateListUiState
import com.example.stallalerterapp.ui.theme.StallAlerterTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Screen displaying the list of imported candidates with priority sorting.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateListScreen(
    navController: NavController = rememberNavController(),
    viewModel: CandidateViewModel = viewModel(factory = CandidateViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    // Keep the last selected URI so "Retry" can re-import without re-picking the file.
    val lastImportUri = rememberSaveable { mutableStateOf<String?>(null) }

    // Storage Access Framework picker: requests a single readable document.
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            lastImportUri.value = uri.toString()
            viewModel.importCsv(uri)
        }
    }

    fun startImport() {
        openDocumentLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stall Alerter") },
                actions = {
                    OutlinedButton(
                        onClick = { startImport() },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Import")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Top
        ) {
            val currentUiState = uiState
            when (currentUiState) {
                is CandidateListUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(40.dp))
                            Text(text = "Loading candidates...")
                        }
                    }
                }

                is CandidateListUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Import failed: ${currentUiState.message}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = {
                                val uri = lastImportUri.value?.let { android.net.Uri.parse(it) }
                                if (uri != null) {
                                    viewModel.importCsv(uri)
                                } else {
                                    startImport()
                                }
                            }
                        ) {
                            Text("Retry")
                        }
                    }
                }

                is CandidateListUiState.Empty -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "No candidates imported yet.\nTap the import button above to select a CSV file.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = { startImport() }) {
                            Text("Import CSV")
                        }
                    }
                }

                is CandidateListUiState.NoData -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "No candidate data found in CSV",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = { startImport() }) {
                            Text("Try Again")
                        }
                    }
                }

                is CandidateListUiState.Success -> {
                    CandidateListContent(
                        candidates = currentUiState.candidates,
                        onCandidateSelect = { candidateUiState ->
                            viewModel.selectCandidate(candidateUiState)
                            navController.navigate("candidateDetail")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CandidateListContent(
    candidates: List<CandidateUiState>,
    onCandidateSelect: (CandidateUiState) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        items(candidates) { candidateUiState ->
            CandidateCard(
                candidateUiState = candidateUiState,
                onSelect = { onCandidateSelect(candidateUiState) }
            )
        }
    }
}

/**
 * Card displaying a single candidate.
 */
@Composable
fun CandidateCard(
    candidateUiState: CandidateUiState,
    onSelect: () -> Unit
) {
    val candidate = candidateUiState.candidate
    val tier = candidateUiState.tier
    val flags = candidateUiState.flags
    val nextAction = candidateUiState.nextAction

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onSelect),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header row: name + stage
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = candidate.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stageString(candidate.stage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                TierBadge(tier = tier)
            }

            // Flags row — wraps naturally so chips don't stretch apart.
            if (flags.isNotEmpty()) {
                FlagChipGroup(flags = flags)
            } else {
                Text(
                    text = "No flags — candidate is on track",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Footer row: last contact + next action, grouped together.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Last contact: ${candidate.lastContact.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (nextAction != null) {
                    NextActionPreview(action = nextAction)
                }
            }
        }
    }
}

/** Chip group displaying flags. Uses a horizontal scroll row so multiple flags
 *  wrap side-by-side without stretching to fill the card width. */
@Composable
fun FlagChipGroup(flags: List<Flag>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(flags) { flag ->
            FlagChip(flag = flag)
        }
    }
}

/** Individual flag chip. */
@Composable
fun FlagChip(flag: Flag) {
    val icon: androidx.compose.ui.graphics.vector.ImageVector
    val contentDescription: String
    val color: Color
    when (flag.flagId) {
        FlagId.HIRED_STALE -> {
            icon = Icons.Default.CheckCircle
            contentDescription = "Hired stale"
            color = Color(0xFF4CAF50)
        }
        FlagId.RECENT_ACTIVITY -> {
            icon = Icons.Default.Info
            contentDescription = "Recent activity"
            color = Color(0xFF2196F3)
        }
        FlagId.STALE_60 -> {
            icon = Icons.Default.Warning
            contentDescription = "Stale 60+"
            color = Color(0xFFFF9800)
        }
        FlagId.STALE_30 -> {
            icon = Icons.Default.Warning
            contentDescription = "Stale 30+"
            color = Color(0xFFFF9800)
        }
        FlagId.STALE_14 -> {
            icon = Icons.Default.Warning
            contentDescription = "Stale 14+"
            color = Color(0xFFFF9800)
        }
        FlagId.STUCK_SCREENING -> {
            icon = Icons.Default.Warning
            contentDescription = "Stuck screening"
            color = Color(0xFFFF5722)
        }
        FlagId.STUCK_TECH -> {
            icon = Icons.Default.Warning
            contentDescription = "Stuck tech"
            color = Color(0xFFFF5722)
        }
        FlagId.OFFER_STALE -> {
            icon = Icons.Default.Error
            contentDescription = "Offer stale"
            color = Color.Red
        }
        else -> {
            icon = Icons.Default.Info
            contentDescription = "Flag"
            color = Color(0xFF6C757D)
        }
    }

    Box(
        modifier = Modifier
            .padding(end = 8.dp, bottom = 8.dp)
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = contentDescription, tint = color, modifier = Modifier.size(16.dp))
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = flag.description,
                    style = MaterialTheme.typography.labelSmall,
                    overflow = TextOverflow.Ellipsis
                )
                if (flag.scoreAdjustment != 0) {
                    Text(
                        text = "(${if (flag.scoreAdjustment > 0) "+" else ""}${flag.scoreAdjustment})",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (flag.scoreAdjustment > 0) Color.Red else Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

/** Next action preview on candidate card — a compact, content-sized pill
 *  so it never stretches to fill the card width. */
@Composable
fun NextActionPreview(action: NextAction) {
    val actionColor = when (action.actionId) {
        ActionId.NO_ACTION_NEEDED -> MaterialTheme.colorScheme.onSurfaceVariant
        ActionId.ESCALATE_TO_HIRING_MANAGER -> Color.Red
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .wrapContentSize()
            .background(actionColor.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "→ ${action.title}",
                style = MaterialTheme.typography.labelSmall,
                color = actionColor,
                fontWeight = FontWeight.Medium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            if (action.subtitle != null) {
                Text(
                    text = action.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = actionColor.copy(alpha = 0.7f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
        }
    }
}

/** Tier badge. */
@Composable
fun TierBadge(tier: PriorityTier) {
    val (text, color) = when (tier) {
        PriorityTier.Critical -> "Critical" to Color.Red
        PriorityTier.High -> "High" to Color(0xFFFF9800)
        PriorityTier.Medium -> "Medium" to Color(0xFFFFC107)
        PriorityTier.Low -> "Low" to Color(0xFF4CAF50)
        PriorityTier.None -> "None" to Color.Gray
    }

    // Pill-shaped tier badge — background fills text bounds, padding expands outward
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = Color.White,
        modifier = Modifier
            .background(color, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    )
}

/** Stage to string conversion. */
private fun stageString(stage: Stage): String {
    return when (stage) {
        Stage.screening -> "Screening"
        Stage.technicalInterview -> "Technical Interview"
        Stage.onSite -> "On-Site"
        Stage.offer -> "Offer"
        Stage.rejected -> "Rejected"
        Stage.hired -> "Hired"
    }
}