package com.example.stallalerterapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stallalerterapp.ui.CandidateListScreen
import com.example.stallalerterapp.ui.CandidateDetailScreen
import com.example.stallalerterapp.viewmodel.CandidateViewModel

/**
 * Main activity for the Stall Alerter application.
 * Hosts the navigation graph with CandidateListScreen and CandidateDetailScreen.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // Activity-scoped ViewModel: shared by the list and detail
                // destinations (a per-destination viewModel() would create a
                // second store with no selected candidate).
                val sharedViewModel: CandidateViewModel = viewModel(
                    viewModelStoreOwner = this,
                    factory = CandidateViewModel.Factory
                )
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController, startDestination = "list") {
                        composable("list") {
                            CandidateListScreen(
                                navController = navController,
                                viewModel = sharedViewModel
                            )
                        }
                        composable("candidateDetail") {
                            CandidateDetailScreen(
                                viewModel = sharedViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}