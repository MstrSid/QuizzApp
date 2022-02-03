package by.kos.quizzapp

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import by.kos.quizzapp.databinding.ActivitySelfControlTheoryBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SelfControlTheoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelfControlTheoryBinding

    private var mCurrentPosition: Int = 1
    private var mQuestionList: ArrayList<Question>? = null
    private var mSelectedOptionPosition: Int = 0
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var tvOptionOne: TextView
    private lateinit var tvOptionTwo: TextView
    private lateinit var tvOptionThree: TextView
    private lateinit var btnSubmit: Button
    private lateinit var fabHome: FloatingActionButton
    private var mCorrectAnswers: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySelfControlTheoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressBar = binding.progressBarS
        tvProgress = binding.tvProgressS
        tvQuestion = binding.tvQuestionS
        tvOptionOne = binding.tvOptionOneS
        tvOptionTwo = binding.tvOptionTwoS
        tvOptionThree = binding.tvOptionThreeS
        btnSubmit = binding.btnSubmitS
        fabHome = binding.fabHomeS

        mQuestionList = Constants.getQuestions()
        progressBar.max = mQuestionList!!.size

        setQuestion()

        tvOptionOne.setOnClickListener {
            selectedOptionView(tvOptionOne, 1)
        }
        tvOptionTwo.setOnClickListener {
            selectedOptionView(tvOptionTwo, 2)
        }
        tvOptionThree.setOnClickListener {
            selectedOptionView(tvOptionThree, 3)
        }
        btnSubmit.setOnClickListener {
            if (btnSubmit.text == getString(R.string.next_question_text) || btnSubmit.text == getString(
                    R.string.test_over_text
                )
            ) {
                mSelectedOptionPosition = 0
            }
            if (mSelectedOptionPosition == 0) {
                mCurrentPosition++

                when {
                    mCurrentPosition <= mQuestionList!!.size -> {
                        setQuestion()
                    }
                    else -> {
                        val intent = Intent(this, ResultActivity::class.java)
                        intent.putExtra(Constants.CORRECT_ANSWERS, mCorrectAnswers)
                        intent.putExtra(Constants.TOTAL_QUESTIONS, mQuestionList!!.size)
                        intent.putExtra(Constants.USER_NAME, "")
                        startActivity(intent)
                        finish()
                    }
                }
            } else {
                val question = mQuestionList?.get(mCurrentPosition - 1)
                if (question!!.correctAnswer != mSelectedOptionPosition) {
                    answerView(mSelectedOptionPosition, R.drawable.wrong_option_border_bg)
                } else {
                    mCorrectAnswers++
                }
                answerView(question.correctAnswer, R.drawable.correct_option_border_bg)
                if (mCurrentPosition == mQuestionList!!.size) {
                    btnSubmit.text = getString(R.string.test_over_text)
                } else {
                    btnSubmit.text = getString(R.string.next_question_text)
                }
                mSelectedOptionPosition = 0
            }
        }
        fabHome.setOnClickListener {
            //AlertDialog.Builder
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }


    }

    private fun setQuestion() {

        val question = mQuestionList!![mCurrentPosition - 1]

        defaultOptionsView()
        btnSubmit.text = getString(R.string.answer_text_btn)

        progressBar.progress = mCurrentPosition
        tvProgress.text = "$mCurrentPosition/" + progressBar.max
        tvQuestion.text = question.question
        tvOptionOne.text = question.optionOne
        tvOptionTwo.text = question.optionTwo
        tvOptionThree.text = question.optionThree
    }

    private fun defaultOptionsView() {
        val options = ArrayList<TextView>()
        options.add(0, tvOptionOne)
        options.add(1, tvOptionTwo)
        options.add(2, tvOptionThree)
        for (option in options) {
            option.setTextColor(Color.parseColor("#7A8089"))
            option.typeface = Typeface.DEFAULT
            option.background = ContextCompat.getDrawable(this, R.drawable.default_option_border_bg)
        }
    }

    private fun answerView(answer: Int, drawableView: Int) {
        when (answer) {
            1 -> {
                tvOptionOne.background = ContextCompat.getDrawable(this, drawableView)
            }
            2 -> {
                tvOptionTwo.background = ContextCompat.getDrawable(this, drawableView)
            }
            3 -> {
                tvOptionThree.background = ContextCompat.getDrawable(this, drawableView)
            }

        }
    }

    private fun selectedOptionView(tv: TextView, selectedOptionNum: Int) {
        defaultOptionsView()
        mSelectedOptionPosition = selectedOptionNum
        tv.setTextColor(Color.parseColor("#7A8089"))
        tv.setTypeface(tv.typeface, Typeface.BOLD)
        tv.background = ContextCompat.getDrawable(this, R.drawable.selected_option_border_bg)
    }

}