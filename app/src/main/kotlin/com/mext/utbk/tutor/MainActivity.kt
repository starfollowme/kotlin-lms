package com.mext.utbk.tutor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mext.utbk.tutor.ui.navigation.AppNavigation
import com.mext.utbk.tutor.ui.theme.MextUtbkTutorTheme
import com.mext.utbk.tutor.viewmodel.*

class MainActivity : ComponentActivity() {
    // Instansiasi ViewModel secara manual untuk menghindari boilerplate DI sederhana
    private val materialViewModel = MaterialViewModel()
    private val quizViewModel = QuizViewModel()
    private val chatViewModel = ChatViewModel()
    private val plannerViewModel = PlannerViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
