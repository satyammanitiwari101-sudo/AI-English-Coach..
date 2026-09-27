package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BottomNavigationBar
import com.example.ui.components.TopHeader
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CoachViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: CoachViewModel = viewModel()
                val currentTab by viewModel.currentTab.collectAsState()
                val profileState by viewModel.profileState.collectAsState()
                val toastMessage by viewModel.toastMessage.collectAsState()
                val showPlacement by viewModel.showPlacementDialog.collectAsState()
                val placementStep by viewModel.placementStep.collectAsState()

                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(toastMessage) {
                    toastMessage?.let { msg ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(msg)
                            viewModel.clearToast()
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        val subtitle = when (currentTab) {
                            "home" -> "Home"
                            "conversation" -> "AI Conversation"
                            "learn" -> "Curriculum & Learn"
                            "progress" -> "Fluency Insights"
                            "profile" -> "Learner Profile"
                            else -> "AI English Coach"
                        }
                        TopHeader(
                            title = "AI English Coach",
                            subtitle = subtitle,
                            streakDays = profileState.dailyStreak,
                            onProfileClick = { viewModel.setTab("profile") }
                        )
                    },
                    bottomBar = {
                        BottomNavigationBar(
                            currentTab = currentTab,
                            onTabSelected = { tab -> viewModel.setTab(tab) }
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            "home" -> HomeScreen(viewModel = viewModel, onNavigateTab = { viewModel.setTab(it) })
                            "conversation" -> ConversationScreen(viewModel = viewModel)
                            "learn" -> LearnScreen(viewModel = viewModel)
                            "progress" -> ProgressScreen(viewModel = viewModel)
                            "profile" -> ProfileScreen(viewModel = viewModel)
                            else -> HomeScreen(viewModel = viewModel, onNavigateTab = { viewModel.setTab(it) })
                        }

                        // Adaptive Placement Assessment Dialog
                        if (showPlacement) {
                            val questions = viewModel.placementQuestions
                            val q = questions.getOrNull(placementStep) ?: questions.last()
                            AlertDialog(
                                onDismissRequest = { },
                                title = {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(text = "Let's find your English level 🎯", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Text(text = "Question ${placementStep + 1} of ${questions.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                                            Text(
                                                text = q.question,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }

                                        q.options.forEachIndexed { index, option ->
                                            Button(
                                                onClick = { viewModel.answerPlacementQuestion(index) },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest, contentColor = MaterialTheme.colorScheme.onSurface)
                                            ) {
                                                Text(text = option, style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }

                                        Text(
                                            text = "💡 No problem. We'll start from the basics. You can improve step by step. Let's build your English together.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                },
                                confirmButton = {}
                            )
                        }
                    }
                }
            }
        }
    }
}
