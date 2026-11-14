package by.kos.quizzapp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import by.kos.quizzapp.databinding.ActivityQuizQuestionsBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject

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
    private lateinit var resultDocument: DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val resultId = savedInstanceState?.getString("result_document_id")
            ?: FirebaseFirestore.getInstance()
                .collection("test_results")
                .document()
                .id

        resultDocument = FirebaseFirestore.getInstance()
            .collection("test_results")
            .document(resultId)

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

        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt("current_position", 1)

            mCorrectAnswers = savedInstanceState.getInt("correct_answers", 0)
            mSelectedOptionPosition = savedInstanceState.getInt("selected_option", 0)
            isTestCompleted = savedInstanceState.getBoolean("test_completed", false)

            @Suppress("DEPRECATION")
            val savedAnswers =
                savedInstanceState.getParcelableArrayList<StudentAnswer>(
                    "student_answers"
                )

            studentAnswers.clear()
            studentAnswers.addAll(savedAnswers ?: emptyList())
        }

        if (isTestCompleted) {
            btnSubmit.text = "Повторить сохранение"
            btnSubmit.isEnabled = true
        } else {
            setQuestion()

            if (mSelectedOptionPosition != 0) {
                val selectedView = when (mSelectedOptionPosition) {
                    1 -> tvOptionOne
                    2 -> tvOptionTwo
                    3 -> tvOptionThree
                    else -> null
                }

                selectedView?.let {
                    selectedOptionView(it, mSelectedOptionPosition)
                }
            }
        }

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

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("result_document_id", resultDocument.id)

        outState.putInt("current_position", mCurrentPosition)
        outState.putInt("correct_answers", mCorrectAnswers)
        outState.putInt("selected_option", mSelectedOptionPosition)
        outState.putBoolean("test_completed", isTestCompleted)

        outState.putParcelableArrayList(
            "student_answers",
            ArrayList(studentAnswers)
        )

        super.onSaveInstanceState(outState)
    }

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

        // Преобразуем ответы для сохранения
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

        // Подготавливаем локальную копию результата
        val localResult = JSONObject().apply {
            put("studentName", mUserName ?: "")
            put("studentGroup", mUserGroup ?: "")
            put("testNumber", Constants.TEST_NUMBER)
            put("correctAnswers", mCorrectAnswers)
            put("totalQuestions", mQuestionList?.size ?: 0)
            put("answers", org.json.JSONArray(answers))
            put("userId", user.uid)
        }

        // Сначала сохраняем результат на устройстве
        val savedLocally = PendingResultStorage.addResult(
            applicationContext,
            resultDocument.id,
            localResult
        )

        if (!savedLocally) {
            isSaving = false
            btnSubmit.isEnabled = true
            btnSubmit.text = "Повторить сохранение"

            Toast.makeText(
                this,
                "Не удалось сохранить результат на устройстве",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        Log.d(
            "PENDING_RESULTS",
            "После локального сохранения: ${
                PendingResultStorage.getResults(applicationContext).size
            }"
        )

        // Передаём отправку единому механизму синхронизации
        PendingResultSync.sync(applicationContext)

        // Не ждём ответа сервера
        isSaving = false

        Toast.makeText(
            this,
            "Результат сохранён на устройстве",
            Toast.LENGTH_SHORT
        ).show()

        onSuccess()
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
