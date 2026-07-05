package com.example.mcqquiz.model

enum class AnswerStatus {
    CORRECT,
    INCORRECT,
    SKIPPED
}

data class QuizState(
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedOption: String? = null,
    val isAnswered: Boolean = false,
    val correctAnswersCount: Int = 0,
    val currentStreak: Int = 0,
    val highestStreak: Int = 0,
    val isLoading: Boolean = true,
    val isCompleted: Boolean = false,
    val questionStatuses: Map<Int, AnswerStatus> = emptyMap()
) {
    val currentQuestion: Question?
        get() = if (currentQuestionIndex in questions.indices) questions[currentQuestionIndex] else null

    val totalQuestionsCount: Int
        get() = questions.size

    val progress: Float
        get() = if (totalQuestionsCount > 0) (currentQuestionIndex + 1).toFloat() / totalQuestionsCount else 0f

    val skippedQuestionsCount: Int
        get() = questionStatuses.values.count { it == AnswerStatus.SKIPPED }
}
