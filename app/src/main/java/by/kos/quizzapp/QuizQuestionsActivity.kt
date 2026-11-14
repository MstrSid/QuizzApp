package by.kos.quizzapp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import by.kos.quizzapp.databinding.ActivityQuizQuestionsBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class QuizQuestionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizQuestionsBinding

    private var mCurrentPosition: Int = 1
    private var mQuestionList: ArrayList<Question>? = null
    private var mSelectedOptionPosition: Int = 0

    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var ivImage: ImageView
    private lateinit var tvOptionOne: TextView
    private lateinit var tvOptionTwo: TextView
    private lateinit var tvOptionThree: TextView
    private lateinit var btnSubmit: Button
    private lateinit var fabHome: FloatingActionButton

    private var mCorrectAnswers: Int = 0
    private val studentAnswers = ArrayList<StudentAnswer>()

    private var mUserName: String? = null
    private var mUserGroup: String? = null

    // Признак завершения теста
    private var isTestCompleted = false

    // Защита от повторного нажатия во время отправки
    private var isSaving = false

    // Один идентификатор документа для повторных отправок
    private val resultDocument = FirebaseFirestore.getInstance()
        .collection("test_results")
        .document()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQuizQuestionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mUserName = StudentSession.getStudentName(this)
        mUserGroup = StudentSession.getStudentGroup(this)

        progressBar = binding.progressBar
        tvProgress = binding.tvProgress
        tvQuestion = binding.tvQuestion
        ivImage = binding.ivImage
        tvOptionOne = binding.tvOptionOne
        tvOptionTwo = binding.tvOptionTwo
        tvOptionThree = binding.tvOptionThree
        btnSubmit = binding.btnSubmit
        fabHome = binding.fabHome

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

            // Повторная отправка завершённого теста
            if (isTestCompleted) {
                saveTestResult {
                    openResultActivity()
                }
                return@setOnClickListener
            }

            // Нельзя подтвердить ответ без выбора варианта
            if (mSelectedOptionPosition == 0) {
                return@setOnClickListener
            }

            val question = mQuestionList!![mCurrentPosition - 1]

            // Запоминаем выбранный ответ
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
                    isCorrect =
                        mSelectedOptionPosition == question.correctAnswer
                )
            )

            // Подсчитываем правильные ответы,
            // не показывая студенту результат
            if (mSelectedOptionPosition == question.correctAnswer) {
                mCorrectAnswers++
            }

            mSelectedOptionPosition = 0

            // Переходим к следующему вопросу
            mCurrentPosition++

            if (mCurrentPosition <= mQuestionList!!.size) {
                setQuestion()
            } else {
                // Все вопросы пройдены
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

    // Переход на экран результатов
    private fun openResultActivity() {

        val intent = Intent(
            this,
            ResultActivity::class.java
        )

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
            mUserName
        )

        intent.putExtra(
            Constants.USER_GROUP,
            mUserGroup
        )

        startActivity(intent)
        finish()
    }

    // Сохранение результата в Firebase Firestore
    private fun saveTestResult(
        onSuccess: () -> Unit
    ) {

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

        // Преобразуем ответы для Firestore
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

        // Формируем документ результата
        val result = hashMapOf<String, Any>(
            "studentName" to (mUserName ?: ""),
            "studentGroup" to (mUserGroup ?: ""),
            "testNumber" to Constants.TEST_NUMBER,
            "correctAnswers" to mCorrectAnswers,
            "totalQuestions" to (mQuestionList?.size ?: 0),
            "completedAt" to FieldValue.serverTimestamp(),
            "answers" to answers,
            "userId" to user.uid
        )

        // Отправляем данные
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

    // Отображение текущего вопроса
    private fun setQuestion() {

        val question =
            mQuestionList!![mCurrentPosition - 1]

        defaultOptionsView()

        btnSubmit.isEnabled = false
        btnSubmit.text =
            getString(R.string.answer_text_btn)

        progressBar.progress = mCurrentPosition

        tvProgress.text =
            "$mCurrentPosition/" + progressBar.max

        tvQuestion.text = question.question

        question.image?.let {
            ivImage.setImageResource(it)
        }

        tvOptionOne.text = question.optionOne
        tvOptionTwo.text = question.optionTwo
        tvOptionThree.text = question.optionThree
    }

    // Сброс оформления вариантов
    private fun defaultOptionsView() {

        val options = arrayListOf(
            tvOptionOne,
            tvOptionTwo,
            tvOptionThree
        )

        for (option in options) {

            option.setTextColor(
                Color.parseColor("#7A8089")
            )

            option.typeface = Typeface.DEFAULT

            option.background =
                ContextCompat.getDrawable(
                    this,
                    R.drawable.default_option_border_bg
                )
        }
    }

    // Выбор варианта ответа
    private fun selectedOptionView(
        tv: TextView,
        selectedOptionNum: Int
    ) {

        defaultOptionsView()

        btnSubmit.isEnabled = true

        mSelectedOptionPosition = selectedOptionNum

        tv.setTextColor(
            Color.parseColor("#7A8089")
        )

        tv.setTypeface(
            tv.typeface,
            Typeface.BOLD
        )

        tv.background =
            ContextCompat.getDrawable(
                this,
                R.drawable.selected_option_border_bg
            )
    }

    // Подтверждение выхода из теста
    override fun onBackPressed() {

        val builder = AlertDialog.Builder(this)

        builder.setTitle(
            getString(R.string.txt_confirm)
        )

        builder.setMessage(
            getString(R.string.txt_exit_question)
        )

        builder.setPositiveButton(
            getString(R.string.exit_yes)
        ) { _, _ ->

            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                )
            )

            finish()
        }

        builder.setNegativeButton(
            getString(R.string.exit_no)
        ) { _, _ -> }

        builder.show()
    }
}
