package com.mext.utbk.tutor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mext.utbk.tutor.data.local.AppDatabase
import com.mext.utbk.tutor.data.remote.ApiClient
import com.mext.utbk.tutor.data.repository.*
import com.mext.utbk.tutor.ui.navigation.AppNavigation
import com.mext.utbk.tutor.ui.theme.MextUtbkTutorTheme
import com.mext.utbk.tutor.viewmodel.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi Database Room
        val database = AppDatabase.getDatabase(applicationContext)

        // Inisialisasi Repositori dengan DAO masing-masing
        val materialRepository = MaterialRepositoryImpl(database.bookmarkDao())
        val quizRepository = QuizRepositoryImpl(database.historyDao())
        val chatRepository = ChatRepositoryImpl(database.chatDao(), ApiClient.service)
        val plannerRepository = PlannerRepositoryImpl(database.studyPlanDao())

        // Inisialisasi ViewModel dengan menyuntikkan Repositori
        val materialViewModel = MaterialViewModel(materialRepository)
        val quizViewModel = QuizViewModel(quizRepository)
        val chatViewModel = ChatViewModel(chatRepository)
        val plannerViewModel = PlannerViewModel(plannerRepository)

        setContent {
            MextUtbkTutorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavigation(
                        materialViewModel = materialViewModel,
                        quizViewModel = quizViewModel,
                        chatViewModel = chatViewModel,
                        plannerViewModel = plannerViewModel
                    )
                }
            }
        }
    }
}
