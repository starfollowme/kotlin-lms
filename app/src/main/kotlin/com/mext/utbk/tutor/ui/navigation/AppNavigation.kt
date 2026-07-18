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
    plannerViewModel: PlannerViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToMaterials = { navController.navigate(Screen.Materials.route) },
                onNavigateToQuiz = { navController.navigate(Screen.Quiz.route) },
                onNavigateToChat = { navController.navigate(Screen.Chat.route) },
                onNavigateToPlanner = { navController.navigate(Screen.Planner.route) },
                onNavigateToSimulation = { navController.navigate(Screen.Simulation.route) }
            )
        }

        composable(Screen.Materials.route) {
            MaterialsScreen(
                viewModel = materialViewModel,
                onNavigateToTopic = { subjectId, topicId ->
                    navController.navigate(Screen.MaterialDetail.createRoute(subjectId, topicId))
                },
                onBack = { navController.popBackStack() }
            )
        }

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
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Quiz.route) {
            QuizScreen(
                viewModel = quizViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Chat.route) {
            AIChatScreen(
                viewModel = chatViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Planner.route) {
            PlannerScreen(
                viewModel = plannerViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Simulation.route) {
            SimulationScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
