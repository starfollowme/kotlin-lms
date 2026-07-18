package com.mext.utbk.tutor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mext.utbk.tutor.ui.screens.*
import com.mext.utbk.tutor.viewmodel.*

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    materialViewModel: MaterialViewModel,
    quizViewModel: QuizViewModel,
    chatViewModel: ChatViewModel,
    plannerViewModel: PlannerViewModel,
    historyViewModel: HistoryViewModel,
    simulationViewModel: SimulationViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "container"
    ) {
        // Halaman container utama yang berisi Bottom Navigation Bar
        composable("container") {
            MainContainerScreen(
                rootNavController = navController,
                materialViewModel = materialViewModel,
                quizViewModel = quizViewModel,
                chatViewModel = chatViewModel,
                plannerViewModel = plannerViewModel,
                historyViewModel = historyViewModel
            )
        }

        // Overlay screens (halaman penuh tanpa bottom navigation bar untuk kenyamanan belajar)
        composable(
            route = Screen.MaterialDetail.route,
            arguments = listOf(
                navArgument("subjectId") { type = NavType.StringType },
                navArgument("topicId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
            val topicId = backStackEntry.arguments?.getString("topicId") ?: ""
            MaterialDetailScreen(
                subjectId = subjectId,
                topicId = topicId,
                viewModel = materialViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToQuiz = { navController.navigate(Screen.Quiz.route) }
            )
        }

        composable(Screen.Quiz.route) {
            QuizScreen(
                viewModel = quizViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Simulation.route) {
            SimulationScreen(
                viewModel = simulationViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
