package by.kos.quizzapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView

class QuizQuestionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_questions)

        val questionList = Constants.getQuestions()
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        val tvProgress = findViewById<TextView>(R.id.tv_progress)
        val tvQuestion = findViewById<TextView>(R.id.tv_question)
        val ivImage = findViewById<ImageView>(R.id.iv_image)
        val tvOptionOne = findViewById<TextView>(R.id.tv_option_one)
        val tvOptionTwo = findViewById<TextView>(R.id.tv_option_two)
        val tvOptionThree = findViewById<TextView>(R.id.tv_option_three)
        Log.i("Question",questionList.toString())

        val currentPosition = 1
        val question: Question? = questionList[currentPosition-1]
        progressBar.progress = currentPosition
        tvProgress.text = "$currentPosition/"+progressBar.max
        tvQuestion.text = question!!.question
        question.image?.let { ivImage.setImageResource(it) }
        tvOptionOne.text = question.optionOne
        tvOptionTwo.text = question.optionTwo
        tvOptionThree.text = question.optionThree
    }
}