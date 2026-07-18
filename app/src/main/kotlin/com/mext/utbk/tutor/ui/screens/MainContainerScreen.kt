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
    historyViewModel: PlannerViewModel
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
                        icon = { Icon(imageVector = icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = nestedNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    materialViewModel = materialViewModel,
                    plannerViewModel = plannerViewModel,
                    onNavigateToMaterials = { nestedNavController.navigate(Screen.Materials.route) },
                    onNavigateToQuiz = { rootNavController.navigate(Screen.Quiz.route) },
                    onNavigateToChat = { nestedNavController.navigate(Screen.Chat.route) },
                    onNavigateToPlanner = { nestedNavController.navigate(Screen.Planner.route) },
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
                HistoryPlaceholderScreen(onBack = { nestedNavController.navigate(Screen.Home.route) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryPlaceholderScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Belajar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Riwayat",
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Riwayat Belajar Anda",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Riwayat chat, latihan soal, dan skor Anda akan dicatat dan ditampilkan lengkap di sini.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
