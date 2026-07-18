package com.mext.utbk.tutor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mext.utbk.tutor.ui.navigation.Screen
import com.mext.utbk.tutor.viewmodel.*

@Composable
fun MainContainerScreen(
    rootNavController: NavHostController,
    materialViewModel: MaterialViewModel,
    quizViewModel: QuizViewModel,
    chatViewModel: ChatViewModel,
    plannerViewModel: PlannerViewModel,
    historyViewModel: HistoryViewModel
) {
    val nestedNavController = rememberNavController()
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Screen.bottomBarScreens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    val icon = when (screen) {
                        Screen.Home -> Icons.Default.Home
                        Screen.Materials -> Icons.Default.Book
                        Screen.Chat -> Icons.Default.Chat
                        Screen.Planner -> Icons.Default.DateRange
                        Screen.History -> Icons.Default.History
                        else -> Icons.Default.Home
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                nestedNavController.navigate(screen.route) {
                                    popUpTo(nestedNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(icon, contentDescription = screen.title) },
                        label = { Text(screen.title) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nestedNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    materialViewModel = materialViewModel,
                    plannerViewModel = plannerViewModel,
                    onNavigateToMaterials = { rootNavController.navigate(Screen.Materials.route) },
                    onNavigateToQuiz = { rootNavController.navigate(Screen.Quiz.route) },
                    onNavigateToChat = { rootNavController.navigate(Screen.Chat.route) },
                    onNavigateToPlanner = { rootNavController.navigate(Screen.Planner.route) },
                    onNavigateToSimulation = { rootNavController.navigate(Screen.Simulation.route) }
                )
            }

            composable(Screen.Materials.route) {
                MaterialsScreen(
                    viewModel = materialViewModel,
                    onNavigateToTopic = { subjectId, topicId ->
                        rootNavController.navigate(Screen.MaterialDetail.createRoute(subjectId, topicId))
                    },
                    onBack = { nestedNavController.navigate(Screen.Home.route) }
                )
            }

            composable(Screen.Chat.route) {
                AIChatScreen(
                    viewModel = chatViewModel,
                    onBack = { nestedNavController.navigate(Screen.Home.route) }
                )
            }

            composable(Screen.Planner.route) {
                PlannerScreen(
                    viewModel = plannerViewModel,
                    onBack = { nestedNavController.navigate(Screen.Home.route) }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = historyViewModel,
                    onBack = { nestedNavController.navigate(Screen.Home.route) }
                )
            }
        }
    }
}
