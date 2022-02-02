package by.kos.quizzapp

data class QuestionMulti(
    val id: Int,
    val question: String,
    val image: Int?,
    val options: ArrayList<String>,
    val correctOptions: ArrayList<Int>
)