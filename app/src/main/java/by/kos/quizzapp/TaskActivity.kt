
package by.kos.quizzapp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import by.kos.quizzapp.databinding.ActivityTaskBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class TaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskBinding

    private var mCurrentPosition = 1
    private var mQuestionList: ArrayList<QuestionMulti>? = null
    private val mSelectedOptionPosition = mutableSetOf<Int>()
    private var mCorrectAnswers = 0

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

    private val studentAnswers = ArrayList<Map<String, Any>>()

    private var isTestCompleted = false
    private var isSaving = false

    private val resultDocument = FirebaseFirestore.getInstance()
        .collection("test_results")
        .document()

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

        setupCheckBox(chOptionOne, 1)
        setupCheckBox(chOptionTwo, 2)
        setupCheckBox(chOptionThree, 3)
        setupCheckBox(chOptionFour, 4)

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

            if (mSelectedOptionPosition.isEmpty()) {
                return@setOnClickListener
            }

            val question = mQuestionList!![mCurrentPosition - 1]

            val selectedAnswers = mSelectedOptionPosition.sorted()
            val correctAnswers = question.correctOptions.sorted()

            val isCorrect = selectedAnswers == correctAnswers

            studentAnswers.add(
                mapOf(
                    "questionId" to mCurrentPosition,
                    "questionText" to question.question,
                    "options" to question.options,
                    "selectedAnswers" to selectedAnswers,
                    "correctAnswers" to correctAnswers,
                    "isCorrect" to isCorrect
                )
            )

            if (isCorrect) {
                mCorrectAnswers++
            }

            mSelectedOptionPosition.clear()
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

    private fun setupCheckBox(
        checkBox: CheckBox,
        optionNumber: Int
    ) {
        checkBox.setOnCheckedChangeListener { _, isChecked ->

            if (isTestCompleted || isSaving) {
                return@setOnCheckedChangeListener
            }

            if (isChecked) {
                mSelectedOptionPosition.add(optionNumber)
            } else {
                mSelectedOptionPosition.remove(optionNumber)
            }

            updateOptionAppearance(checkBox, isChecked)

            btnSubmit.isEnabled = mSelectedOptionPosition.isNotEmpty()
        }
    }

    private fun updateOptionAppearance(
        checkBox: CheckBox,
        isSelected: Boolean
    ) {
        checkBox.setTextColor(Color.parseColor("#7A8089"))

        if (isSelected) {
            checkBox.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            checkBox.background = ContextCompat.getDrawable(
                this,
                R.drawable.selected_option_border_bg
            )
        } else {
            checkBox.typeface = Typeface.DEFAULT
            checkBox.background = ContextCompat.getDrawable(
                this,
                R.drawable.default_option_border_bg
            )
        }
    }

    private fun setQuestion() {

        val question = mQuestionList!![mCurrentPosition - 1]

        mSelectedOptionPosition.clear()

        val checkBoxes = listOf(
            chOptionOne,
            chOptionTwo,
            chOptionThree,
            chOptionFour
        )

        checkBoxes.forEach { checkBox ->
            checkBox.isChecked = false
            updateOptionAppearance(checkBox, false)
        }

        btnSubmit.isEnabled = false
        btnSubmit.text = getString(R.string.answer_text_btn)

        progressBar.progress = mCurrentPosition
        tvProgress.text = "$mCurrentPosition/${progressBar.max}"

        tvQuestion.text = question.question

        if (question.image != null) {
            ivImage.visibility = View.VISIBLE
            ivImage.setImageResource(question.image)
        } else {
            ivImage.visibility = View.GONE
        }

        chOptionOne.text = question.options[0]
        chOptionTwo.text = question.options[1]
        chOptionThree.text = question.options[2]
        chOptionFour.text = question.options[3]
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

        val result = hashMapOf<String, Any>(
            "studentName" to StudentSession.getStudentName(this),
            "studentGroup" to StudentSession.getStudentGroup(this),
            "testNumber" to 4,
            "correctAnswers" to mCorrectAnswers,
            "totalQuestions" to mQuestionList!!.size,
            "completedAt" to FieldValue.serverTimestamp(),
            "answers" to studentAnswers,
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

    override fun onBackPressed() {

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.txt_confirm))
            .setMessage(getString(R.string.txt_exit_question))
            .setPositiveButton(getString(R.string.exit_yes)) { _, _ ->
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .setNegativeButton(getString(R.string.exit_no), null)
            .show()
    }
}
