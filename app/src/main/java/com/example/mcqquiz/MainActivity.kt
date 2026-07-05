package com.example.mcqquiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mcqquiz.data.QuestionRepository
import com.example.mcqquiz.ui.screens.QuizScreen
import com.example.mcqquiz.ui.screens.ResultsScreen
import com.example.mcqquiz.ui.screens.SplashScreen
import com.example.mcqquiz.ui.theme.McqQuizTheme
import com.example.mcqquiz.viewmodel.QuizViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = QuestionRepository(applicationContext)
        val viewModelFactory = QuizViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[QuizViewModel::class.java]

        setContent {
            McqQuizTheme {
                val state = viewModel.state.value

                when {
                    state.isLoading -> {
                        SplashScreen()
                    }
                    state.isCompleted -> {
                        ResultsScreen(
                            state = state,
                            onRestart = { viewModel.restartQuiz() },
                            onClose = { finish() }
                        )
                    }
                    else -> {
                        QuizScreen(
                            state = state,
                            onOptionSelected = { option -> viewModel.selectOption(option) },
                            onSkip = { viewModel.skipQuestion() },
                            onExit = { finish() }
                        )
                    }
                }
            }
        }
    }
}

class QuizViewModelFactory(private val repository: QuestionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuizViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}