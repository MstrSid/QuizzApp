package by.kos.quizzapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StudentAnswer(
    val questionId: Int,
    val questionText: String,
    val options: List<String>,
    val selectedAnswer: Int,
    val correctAnswer: Int,
    val isCorrect: Boolean
) : Parcelable