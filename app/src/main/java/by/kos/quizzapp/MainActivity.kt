package by.kos.quizzapp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnStart = findViewById<Button>(R.id.btn_start)
        val btnStart2 = findViewById<Button>(R.id.btn_start2)
        val etName = findViewById<EditText>(R.id.et_name)
        val btnLearn = findViewById<Button>(R.id.btn_learn)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        btnStart.setOnClickListener {
            if (etName.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Введите имя, пожалуйста.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, QuizQuestionsActivity::class.java)
                intent.putExtra(Constants.USER_NAME, etName.text.toString())
                Constants.TEST_NUMBER = 1
                startActivity(intent)
                finish()
            }
        }

        btnStart2.setOnClickListener {
            if (etName.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Введите имя, пожалуйста.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, QuizQuestionsActivity::class.java)
                intent.putExtra(Constants.USER_NAME, etName.text.toString())
                Constants.TEST_NUMBER = 2
                startActivity(intent)
                finish()
            }
        }

        btnLearn.setOnClickListener{
            val intent = Intent(this, LearnMaterialsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}