package com.example.stallalerterapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stallalerterapp.model.ActionId
import com.example.stallalerterapp.model.Flag
import com.example.stallalerterapp.model.FlagId
import com.example.stallalerterapp.model.NextAction
import com.example.stallalerterapp.model.PriorityTier
import com.example.stallalerterapp.viewmodel.CandidateViewModel
import com.example.stallalerterapp.viewmodel.CandidateUiState
import java.time.format.DateTimeFormatter

/**
 * Detail screen for a single candidate.
 * Shows full candidate info, all flags, priority tier, and recommended next action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateDetailScreen(
    viewModel: CandidateViewModel = viewModel(factory = CandidateViewModel.Factory),
    onBack: () -> Unit
) {
    val detailState by viewModel.detailState.collectAsState()
    val nextAction by viewModel.nextAction.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Candidate Details") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { padding ->
        CandidateDetailContent(
            modifier = Modifier.padding(padding),
            candidateUiState = detailState,
            nextAction = nextAction,
        )
    }
}

@Composable
private fun CandidateDetailContent(
    modifier: Modifier = Modifier,
    candidateUiState: CandidateUiState?,
    nextAction: NextAction?,
) {
    if (candidateUiState == null) return
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header card with candidate info
        item {
            CandidateHeaderCard(candidateUiState = candidateUiState)
        }

        // Flags section
        item {
            if (candidateUiState.flags.isNotEmpty()) {
                FlagsSection(flags = candidateUiState.flags)
            } else {
                NoFlagsCard()
            }
        }

        // Score breakdown
        item {
            ScoreBreakdownCard(candidateUiState = candidateUiState)
        }

        // Recommended action
        item {
            if (nextAction != null) {
                NextActionCard(
                    nextAction = nextAction,
                    tier = candidateUiState.tier
                )
            }
        }
    }
}

@Composable
private fun CandidateHeaderCard(candidateUiState: CandidateUiState) {
    val candidate = candidateUiState.candidate
    val tier = candidateUiState.tier

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = tierColor(tier).copy(alpha = 0.15f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = candidate.fullName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatStage(candidate.stage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                DetailTierBadge(tier = tier)
            }

            // Contact info — show available items vertically to avoid large gaps
            val email = candidate.email?.takeIf { it.isNotBlank() }
            val phone = candidate.phone?.takeIf { it.isNotBlank() }
            if (email != null || phone != null) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    email?.let {
                        InfoItem(
                            icon = Icons.Default.Mail,
                            label = "Email",
                            value = it
                        )
                    }
                    phone?.let {
                        InfoItem(
                            icon = Icons.Default.Phone,
                            label = "Phone",
                            value = it
                        )
                    }
                }
            }

            InfoItem(
                icon = Icons.Default.Info,
                label = "Last Contact",
                value = candidate.lastContact.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
            )
        }
    }
}

@Composable
private fun DetailTierBadge(tier: PriorityTier) {
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

@Composable
private fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun FlagsSection(flags: List<Flag>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Flags", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            flags.forEach { flag ->
                FlagRow(flag = flag)
            }
        }
    }
}

@Composable
private fun FlagRow(flag: Flag) {
    val icon: androidx.compose.ui.graphics.vector.ImageVector
    val color: Color
    when (flag.flagId) {
        FlagId.HIRED_STALE -> {
            icon = Icons.Default.CheckCircle
            color = Color(0xFF4CAF50)
        }
        FlagId.RECENT_ACTIVITY -> {
            icon = Icons.Default.Info
            color = Color(0xFF2196F3)
        }
        FlagId.STALE_60, FlagId.STALE_30, FlagId.STALE_14 -> {
            icon = Icons.Default.Warning
            color = Color(0xFFFF9800)
        }
        FlagId.STUCK_SCREENING, FlagId.STUCK_TECH -> {
            icon = Icons.Default.Warning
            color = Color(0xFFFF5722)
        }
        FlagId.OFFER_STALE -> {
            icon = Icons.Default.Error
            color = Color.Red
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Column {
            Text(
                text = flag.description,
                style = MaterialTheme.typography.bodyMedium
            )
            if (flag.scoreAdjustment != 0) {
                Text(
                    text = "Score adjustment: ${if (flag.scoreAdjustment > 0) "+" else ""}${flag.scoreAdjustment}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (flag.scoreAdjustment > 0) Color.Red else Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
private fun NoFlagsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Text(
            text = "No flags — candidate is on track",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun ScoreBreakdownCard(candidateUiState: CandidateUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Priority Score Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            ScoreRow("Base Score", candidateUiState.baseScore)
            ScoreRow("Flag Adjustments", candidateUiState.flagAdjustment)
            Divider()
            ScoreRow("Adjusted Score", candidateUiState.adjustedScore, isTotal = true)
        }
    }
}

@Composable
private fun ScoreRow(label: String, value: Int, isTotal: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isTotal) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            else MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value.toString(),
            style = if (isTotal) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            else MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
private fun NextActionCard(
    nextAction: NextAction,
    tier: PriorityTier
) {
    val actionColor = when (nextAction.actionId) {
        ActionId.NO_ACTION_NEEDED -> MaterialTheme.colorScheme.onSurfaceVariant
        ActionId.ESCALATE_TO_HIRING_MANAGER -> Color.Red
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = actionColor.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recommended Action",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                // Action type badge using a styled Text instead of Chip (Chip is internal)
                Text(
                    text = formatActionId(nextAction.actionId),
                    style = MaterialTheme.typography.labelSmall,
                    color = actionColor,
                    modifier = Modifier
                        .background(actionColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Text(
                text = nextAction.title,
                style = MaterialTheme.typography.headlineSmall,
                color = actionColor,
                fontWeight = FontWeight.Bold
            )

            nextAction.subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatStage(stage: com.example.stallalerterapp.data.csv.Stage): String {
    return when (stage) {
        com.example.stallalerterapp.data.csv.Stage.screening -> "Screening"
        com.example.stallalerterapp.data.csv.Stage.technicalInterview -> "Technical Interview"
        com.example.stallalerterapp.data.csv.Stage.onSite -> "On-Site"
        com.example.stallalerterapp.data.csv.Stage.offer -> "Offer"
        com.example.stallalerterapp.data.csv.Stage.rejected -> "Rejected"
        com.example.stallalerterapp.data.csv.Stage.hired -> "Hired"
    }
}

private fun formatActionId(actionId: ActionId): String {
    return when (actionId) {
        ActionId.SEND_FOLLOWUP_EMAIL -> "Email"
        ActionId.SCHEDULE_PHONE_SCREEN -> "Phone Screen"
        ActionId.SEND_TECHNICAL_ASSIGNMENT -> "Tech Assignment"
        ActionId.SCHEDULE_FOLLOWUP_INTERVIEW -> "Follow-up Interview"
        ActionId.SEND_THANK_YOU_NOTE -> "Thank You"
        ActionId.SCHEDULE_ONSITE_FOLLOWUP -> "On-site Follow-up"
        ActionId.ESCALATE_TO_HIRING_MANAGER -> "Escalate"
        ActionId.EXTEND_OFFER_DEADLINE -> "Extend Offer"
        ActionId.NO_ACTION_NEEDED -> "No Action"
    }
}

private fun tierColor(tier: PriorityTier): Color {
    return when (tier) {
        PriorityTier.Critical -> Color.Red
        PriorityTier.High -> Color(0xFFFF9800)
        PriorityTier.Medium -> Color(0xFFFFC107)
        PriorityTier.Low -> Color(0xFF4CAF50)
        PriorityTier.None -> Color.Gray
    }
}
