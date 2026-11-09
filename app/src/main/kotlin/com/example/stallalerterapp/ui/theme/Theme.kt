package com.example.stallalerterapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight

/**
 * Material 3 theming constants for Stall Alerter App.
 * Provides corner radii, typography weights, and color constants
 * that complement the base Theme.StallAlerterApp definition.
 */
object StallAlerterTheme {

    /** Corner radii for different UI elements */
    object Shapes {
        val screen: RoundedCornerShape = RoundedCornerShape(16)
        val card: RoundedCornerShape = RoundedCornerShape(12)
        val small: RoundedCornerShape = RoundedCornerShape(8)
        val pill: RoundedCornerShape = RoundedCornerShape(20)
    }

    /** Typography weight extensions */
    object Typography {
        val headerLarge: FontWeight = FontWeight.Bold
        val headerMedium: FontWeight = FontWeight.Medium
        val bodyMedium: FontWeight = FontWeight.Normal
        val labelSmall: FontWeight = FontWeight.Normal
        val labelLarge: FontWeight = FontWeight.Bold
    }

    /** Pre-defined tier colors matching the PriorityTier enum */
    object TierColors {
        val Critical = Color.Red
        val High = Color(0xFFFF9800)
        val Medium = Color(0xFFFFC107)
        val Low = Color(0xFF4CAF50)
        val None = Color.Gray
    }

    /** Returns color for a given PriorityTier */
    fun tierColor(tier: com.example.stallalerterapp.model.PriorityTier): Color {
        return when (tier) {
            com.example.stallalerterapp.model.PriorityTier.Critical -> TierColors.Critical
            com.example.stallalerterapp.model.PriorityTier.High -> TierColors.High
            com.example.stallalerterapp.model.PriorityTier.Medium -> TierColors.Medium
            com.example.stallalerterapp.model.PriorityTier.Low -> TierColors.Low
            com.example.stallalerterapp.model.PriorityTier.None -> TierColors.None
        }
    }
}