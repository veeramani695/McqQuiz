package com.example.mcqquiz.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mcqquiz.data.QuestionRepository
import com.example.mcqquiz.model.AnswerStatus
import com.example.mcqquiz.model.QuizState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class QuizViewModel(private val repository: QuestionRepository) : ViewModel() {

    private val _state = mutableStateOf(QuizState())
    val state: State<QuizState> = _state

    private var autoAdvanceJob: Job? = null

    init {
        loadQuiz()
    }

    fun loadQuiz() {
        _state.value = QuizState(isLoading = true)
        viewModelScope.launch {
            // Simulate network/database fetch delay
            delay(1500)
            val questions = repository.loadQuestionsFromAssets()
            _state.value = QuizState(
                questions = questions,
                isLoading = false
            )
        }
    }

    fun selectOption(option: String) {
        val currentState = _state.value
        if (currentState.isAnswered || currentState.isLoading || currentState.isCompleted) return

        val currentQuestion = currentState.currentQuestion ?: return
        val isCorrect = option == currentQuestion.correctAnswer

        val newStreak = if (isCorrect) currentState.currentStreak + 1 else 0
        val newHighestStreak = if (newStreak > currentState.highestStreak) newStreak else currentState.highestStreak
        val newCorrectCount = if (isCorrect) currentState.correctAnswersCount + 1 else currentState.correctAnswersCount

        val updatedStatuses = currentState.questionStatuses.toMutableMap().apply {
            put(currentQuestion.id, if (isCorrect) AnswerStatus.CORRECT else AnswerStatus.INCORRECT)
        }

        _state.value = currentState.copy(
            selectedOption = option,
            isAnswered = true,
            correctAnswersCount = newCorrectCount,
            currentStreak = newStreak,
            highestStreak = newHighestStreak,
            questionStatuses = updatedStatuses
        )

        // Auto advance to next question after 2 seconds
        autoAdvanceJob?.cancel()
        autoAdvanceJob = viewModelScope.launch {
            delay(2000)
            advanceToNextQuestion()
        }
    }

    fun skipQuestion() {
        val currentState = _state.value
        // If already selected, do not allow skipping
        if (currentState.isAnswered || currentState.isLoading || currentState.isCompleted) return

        val currentQuestion = currentState.currentQuestion ?: return
        val updatedStatuses = currentState.questionStatuses.toMutableMap().apply {
            put(currentQuestion.id, AnswerStatus.SKIPPED)
        }

        autoAdvanceJob?.cancel()

        _state.value = currentState.copy(
            currentStreak = 0, // Reset streak on skip
            questionStatuses = updatedStatuses
        )

        advanceToNextQuestion()
    }

    private fun advanceToNextQuestion() {
        val currentState = _state.value
        val nextIndex = currentState.currentQuestionIndex + 1

        if (nextIndex < currentState.totalQuestionsCount) {
            _state.value = currentState.copy(
                currentQuestionIndex = nextIndex,
                selectedOption = null,
                isAnswered = false
            )
        } else {
            _state.value = currentState.copy(
                isCompleted = true
            )
        }
    }

    fun restartQuiz() {
        autoAdvanceJob?.cancel()
        val currentState = _state.value
        _state.value = QuizState(
            questions = currentState.questions,
            isLoading = false,
            highestStreak = 0 // Reset highest streak on manual restart, or keep session highest? Let's reset as per "reset all counters"
        )
    }
}
