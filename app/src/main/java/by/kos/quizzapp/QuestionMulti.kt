package by.kos.quizzapp

data class QuestionMulti(
    val id: Int,
    val question: String,
    val image: Int?,
    val options: List<String>,
    val correctOptions: List<Int>
)