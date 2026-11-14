
package by.kos.quizzapp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import by.kos.quizzapp.databinding.ActivitySelfControlTheoryBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SelfControlTheoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelfControlTheoryBinding

    private var mCurrentPosition = 1
    private var mQuestionList: ArrayList<Question>? = null
    private var mSelectedOptionPosition = 0
    private var mCorrectAnswers = 0

    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var tvOptionOne: TextView
    private lateinit var tvOptionTwo: TextView
    private lateinit var tvOptionThree: TextView
    private lateinit var btnSubmit: Button
    private lateinit var fabHome: FloatingActionButton

    private val studentAnswers = ArrayList<StudentAnswer>()

    private var isTestCompleted = false
    private var isSaving = false

    private val resultDocument = FirebaseFirestore.getInstance()
        .collection("test_results")
        .document()

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
            if (!isTestCompleted) {
                selectedOptionView(tvOptionOne, 1)
            }
        }

        tvOptionTwo.setOnClickListener {
            if (!isTestCompleted) {
                selectedOptionView(tvOptionTwo, 2)
            }
        }

        tvOptionThree.setOnClickListener {
            if (!isTestCompleted) {
                selectedOptionView(tvOptionThree, 3)
            }
        }

        btnSubmit.setOnClickListener {

            if (isSaving) {
                return@setOnClickListener
            }

            if (isTestCompleted) {
                saveTestResult {
                    openResultActivity()
                }
                return@setOnClickListener
            }

            if (mSelectedOptionPosition == 0) {
                return@setOnClickListener
            }

            val question = mQuestionList!![mCurrentPosition - 1]

            studentAnswers.add(
                StudentAnswer(
                    questionId = question.id,
                    questionText = question.question,
                    options = listOf(
                        question.optionOne,
                        question.optionTwo,
                        question.optionThree
                    ),
                    selectedAnswer = mSelectedOptionPosition,
                    correctAnswer = question.correctAnswer,
                    isCorrect = mSelectedOptionPosition == question.correctAnswer
                )
            )

            if (mSelectedOptionPosition == question.correctAnswer) {
                mCorrectAnswers++
            }

            mSelectedOptionPosition = 0
            mCurrentPosition++

            if (mCurrentPosition <= mQuestionList!!.size) {
                setQuestion()
            } else {
                isTestCompleted = true

                saveTestResult {
                    openResultActivity()
                }
            }
        }

        fabHome.setOnClickListener {
            onBackPressed()
        }
    }

    private fun openResultActivity() {

        val intent = Intent(this, ResultActivity::class.java)

        intent.putExtra(
            Constants.CORRECT_ANSWERS,
            mCorrectAnswers
        )

        intent.putExtra(
            Constants.TOTAL_QUESTIONS,
            mQuestionList!!.size
        )

        intent.putExtra(
            Constants.USER_NAME,
            StudentSession.getStudentName(this)
        )

        intent.putExtra(
            Constants.USER_GROUP,
            StudentSession.getStudentGroup(this)
        )

        startActivity(intent)
        finish()
    }

    private fun saveTestResult(onSuccess: () -> Unit) {

        if (isSaving) {
            return
        }

        isSaving = true
        btnSubmit.isEnabled = false

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            isSaving = false
            btnSubmit.isEnabled = true
            btnSubmit.text = "Повторить сохранение"

            Toast.makeText(
                this,
                "Ошибка авторизации Firebase",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val answers = studentAnswers.map { answer ->
            mapOf(
                "questionId" to answer.questionId,
                "questionText" to answer.questionText,
                "options" to answer.options,
                "selectedAnswer" to answer.selectedAnswer,
                "correctAnswer" to answer.correctAnswer,
                "isCorrect" to answer.isCorrect
            )
        }

        val result = hashMapOf<String, Any>(
            "studentName" to StudentSession.getStudentName(this),
            "studentGroup" to StudentSession.getStudentGroup(this),
            "testNumber" to 3,
            "correctAnswers" to mCorrectAnswers,
            "totalQuestions" to mQuestionList!!.size,
            "completedAt" to FieldValue.serverTimestamp(),
            "answers" to answers,
            "userId" to user.uid
        )

        resultDocument
            .set(result)
            .addOnSuccessListener {
                isSaving = false

                Toast.makeText(
                    this,
                    "Результат успешно сохранён",
                    Toast.LENGTH_SHORT
                ).show()

                onSuccess()
            }
            .addOnFailureListener { exception ->
                isSaving = false
                btnSubmit.isEnabled = true
                btnSubmit.text = "Повторить сохранение"

                Toast.makeText(
                    this,
                    "Ошибка сохранения: ${exception.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun setQuestion() {

        val question = mQuestionList!![mCurrentPosition - 1]

        defaultOptionsView()

        btnSubmit.isEnabled = false
        btnSubmit.text = getString(R.string.answer_text_btn)

        progressBar.progress = mCurrentPosition
        tvProgress.text = "$mCurrentPosition/${progressBar.max}"

        tvQuestion.text = question.question
        tvOptionOne.text = question.optionOne
        tvOptionTwo.text = question.optionTwo
        tvOptionThree.text = question.optionThree
    }

    private fun defaultOptionsView() {

        val options = arrayListOf(
            tvOptionOne,
            tvOptionTwo,
            tvOptionThree
        )

        for (option in options) {
            option.setTextColor(Color.parseColor("#7A8089"))
            option.typeface = Typeface.DEFAULT
            option.background = ContextCompat.getDrawable(
                this,
                R.drawable.default_option_border_bg
            )
        }
    }

    private fun selectedOptionView(
        tv: TextView,
        selectedOptionNum: Int
    ) {
        defaultOptionsView()

        btnSubmit.isEnabled = true
        mSelectedOptionPosition = selectedOptionNum

        tv.setTextColor(Color.parseColor("#7A8089"))
        tv.setTypeface(tv.typeface, Typeface.BOLD)

        tv.background = ContextCompat.getDrawable(
            this,
            R.drawable.selected_option_border_bg
        )
    }

    override fun onBackPressed() {

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.txt_confirm))
            .setMessage(getString(R.string.txt_exit_question))
            .setPositiveButton(getString(R.string.exit_yes)) { _, _ ->
                startActivity(
                    Intent(this, MainActivity::class.java)
                )
                finish()
            }
            .setNegativeButton(getString(R.string.exit_no), null)
            .show()
    }
}
