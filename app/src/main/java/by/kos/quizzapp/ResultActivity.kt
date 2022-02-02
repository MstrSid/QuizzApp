package by.kos.quizzapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import by.kos.quizzapp.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val tvUsername = binding.tvUsername
        val tvScore = binding.tvScore
        val btnFinish = binding.btnFinish

        val username = intent.getStringExtra(Constants.USER_NAME)
        val totalQuestions = intent.getIntExtra(Constants.TOTAL_QUESTIONS, 0)
        val correctAnswers = intent.getIntExtra(Constants.CORRECT_ANSWERS, 0)
        tvUsername.text = username
        tvScore.text = "Правильных ответов - $correctAnswers из $totalQuestions"
        btnFinish.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}