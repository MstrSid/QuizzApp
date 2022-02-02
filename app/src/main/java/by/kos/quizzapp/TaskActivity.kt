package by.kos.quizzapp

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.core.content.ContextCompat
import by.kos.quizzapp.databinding.ActivityTaskBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskBinding
    private var mCurrentPosition: Int = 1
    private var mQuestionList: ArrayList<QuestionMulti>? = null
    private var mSelectedOptionPosition = ArrayList<Int>()
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var ivImage: ImageView
    private lateinit var chOptionOne: CheckBox
    private lateinit var chOptionTwo: CheckBox
    private lateinit var chOptionThree: CheckBox
    private lateinit var chOptionFour: CheckBox
    private lateinit var btnSubmit: Button
    private lateinit var fabHome: FloatingActionButton
    private var mCorrectAnswers: Int = 0
    private var mUserName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressBar = binding.progressBar
        tvProgress = binding.tvProgress
        tvQuestion = binding.tvQuestion
        ivImage = binding.ivImage
        chOptionOne = binding.opt1
        chOptionTwo = binding.opt2
        chOptionThree = binding.opt3
        chOptionFour = binding.opt4
        btnSubmit = binding.btnSubmit
        fabHome = binding.fabHomeTask
        mQuestionList = Constants.getTasks()
        progressBar.max = mQuestionList!!.size

        setQuestion()

        chOptionOne.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedOptionView(chOptionOne, 1)
                mSelectedOptionPosition.add(1)
            } else {
                mSelectedOptionPosition.remove(1)
            }
        }
        chOptionTwo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedOptionView(chOptionTwo, 2)
                mSelectedOptionPosition.add(2)
            } else {
                mSelectedOptionPosition.remove(2)
            }
        }
        chOptionThree.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedOptionView(chOptionThree, 3)
                mSelectedOptionPosition.add(3)
            } else {
                mSelectedOptionPosition.remove(3)
            }
        }

        chOptionFour.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedOptionView(chOptionFour, 4)
                mSelectedOptionPosition.add(4)
            } else {
                mSelectedOptionPosition.remove(4)
            }
        }

        btnSubmit.setOnClickListener {
            if (btnSubmit.text == getString(R.string.next_question_text) || btnSubmit.text == getString(
                    R.string.test_over_text
                )
            ) {
                mSelectedOptionPosition = ArrayList()
            }
            if (mSelectedOptionPosition.size == 0) {
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
                    }
                }
            } else {
                val question = mQuestionList?.get(mCurrentPosition - 1)
                if (question != null) {
                    mSelectedOptionPosition.forEach { item ->
                        question.correctOptions.forEach { answer ->
                            if (item != answer) {
                                answerView(item, R.drawable.wrong_option_border_bg)
                            }
                        }
                    }
                    if (question.correctOptions == mSelectedOptionPosition) {
                        mCorrectAnswers++
                    }
                    question.correctOptions.forEach { item ->
                        answerView(
                            item,
                            R.drawable.correct_option_border_bg
                        )
                    }
                }

                if (mCurrentPosition == mQuestionList!!.size) {
                    btnSubmit.text = getString(R.string.test_over_text)
                } else {
                    btnSubmit.text = getString(R.string.next_question_text)
                }
                mSelectedOptionPosition = ArrayList()
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
        chOptionOne.isChecked = false
        chOptionTwo.isChecked = false
        chOptionThree.isChecked = false
        chOptionFour.isChecked = false
        btnSubmit.text = getString(R.string.answer_text_btn)

        progressBar.progress = mCurrentPosition
        tvProgress.text = "$mCurrentPosition/" + progressBar.max
        tvQuestion.text = question.question
        question.image?.let { ivImage.setImageResource(it) }
        chOptionOne.text = question.options[0]
        chOptionTwo.text = question.options[1]
        chOptionThree.text = question.options[2]
        chOptionFour.text = question.options[3]
    }

    private fun defaultOptionsView() {
        val options = ArrayList<TextView>()
        options.add(0, chOptionOne)
        options.add(1, chOptionTwo)
        options.add(2, chOptionThree)
        options.add(3, chOptionFour)
        for (option in options) {
            option.setTextColor(Color.parseColor("#7A8089"))
            option.typeface = Typeface.DEFAULT
            option.background = ContextCompat.getDrawable(this, R.drawable.default_option_border_bg)
        }
    }

    private fun answerView(answer: Int, drawableView: Int) {
        when (answer) {
            1 -> {
                chOptionOne.background = ContextCompat.getDrawable(this, drawableView)
            }
            2 -> {
                chOptionTwo.background = ContextCompat.getDrawable(this, drawableView)
            }
            3 -> {
                chOptionThree.background = ContextCompat.getDrawable(this, drawableView)
            }
            4 -> {
                chOptionFour.background = ContextCompat.getDrawable(this, drawableView)
            }
        }
    }

    private fun selectedOptionView(ch: CheckBox, selectedOptionNum: Int) {
        defaultOptionsView()
        //mSelectedOptionPosition = selectedOptionNum
        ch.setTextColor(Color.parseColor("#7A8089"))
        ch.setTypeface(ch.typeface, Typeface.BOLD)
        ch.background = ContextCompat.getDrawable(this, R.drawable.selected_option_border_bg)
    }
}