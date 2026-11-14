
package by.kos.quizzapp

data class StudentAnswer(
    val questionId: Int,
    val questionText: String,
    val options: List<String>,
    val selectedAnswer: Int,
    val correctAnswer: Int,
    val isCorrect: Boolean
)
