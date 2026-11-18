package by.kos.quizzapp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import by.kos.quizzapp.databinding.ActivitySelfControlTheoryBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.json.JSONObject

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
    private var isAnswerChecked = false

    // Документ Firestore для текущей попытки
    private lateinit var resultDocument: DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Восстанавливаем идентификатор документа
        val resultId = savedInstanceState
            ?.getString("result_document_id")
            ?: FirebaseFirestore.getInstance()
                .collection("test_results")
                .document()
                .id

        resultDocument = FirebaseFirestore.getInstance()
            .collection("test_results")
            .document(resultId)

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

        // Восстанавливаем состояние тестирования
        if (savedInstanceState != null) {

            mCurrentPosition = savedInstanceState.getInt(
                "current_position",
                1
            )

            mCorrectAnswers = savedInstanceState.getInt(
                "correct_answers",
                0
            )

            mSelectedOptionPosition = savedInstanceState.getInt(
                "selected_option",
                0
            )

            isTestCompleted = savedInstanceState.getBoolean(
                "test_completed",
                false
            )
            isAnswerChecked = savedInstanceState.getBoolean("answer_checked", false)

            @Suppress("DEPRECATION")
            val savedAnswers =
                savedInstanceState.getParcelableArrayList<StudentAnswer>(
                    "student_answers"
                )

            studentAnswers.clear()
            studentAnswers.addAll(savedAnswers ?: emptyList())
        }

        // Отображаем восстановленный вопрос
        if (isTestCompleted) {

            btnSubmit.text = "Повторить сохранение"
            btnSubmit.isEnabled = true

            progressBar.progress = progressBar.max
            tvProgress.text = "${progressBar.max}/${progressBar.max}"

            tvQuestion.text = "Тестирование завершено"

            tvOptionOne.text = ""
            tvOptionTwo.text = ""
            tvOptionThree.text = ""

        } else {

            val selectedPosition = mSelectedOptionPosition

            setQuestion()

            // Восстанавливаем выделенный вариант
            if (selectedPosition != 0) {

                val selectedView = when (selectedPosition) {
                    1 -> tvOptionOne
                    2 -> tvOptionTwo
                    3 -> tvOptionThree
                    else -> null
                }

                selectedView?.let {
                    selectedOptionView(it, selectedPosition)
                }
            }
            if (isAnswerChecked) {
                showAnswerFeedback()
                updateCheckedButtonText()
            }
        }

        // Выбор первого варианта
        tvOptionOne.setOnClickListener {
            if (!isTestCompleted && !isSaving && !isAnswerChecked) {
                selectedOptionView(tvOptionOne, 1)
            }
        }

        // Выбор второго варианта
        tvOptionTwo.setOnClickListener {
            if (!isTestCompleted && !isSaving && !isAnswerChecked) {
                selectedOptionView(tvOptionTwo, 2)
            }
        }

        // Выбор третьего варианта
        tvOptionThree.setOnClickListener {
            if (!isTestCompleted && !isSaving && !isAnswerChecked) {
                selectedOptionView(tvOptionThree, 3)
            }
        }

        // Подтверждение ответа
        btnSubmit.setOnClickListener {

            if (isSaving) {
                return@setOnClickListener
            }

            // Повторное сохранение завершённого теста
            if (isTestCompleted) {
                saveTestResult {
                    openResultActivity()
                }
                return@setOnClickListener
            }

            if (isAnswerChecked) {
                isAnswerChecked = false
                mSelectedOptionPosition = 0
                mCurrentPosition++
                if (mCurrentPosition <= mQuestionList!!.size) {
                    setQuestion()
                } else {
                    isTestCompleted = true
                    saveTestResult { openResultActivity() }
                }
                return@setOnClickListener
            }

            // Ответ не выбран
            if (mSelectedOptionPosition == 0) {
                return@setOnClickListener
            }

            val question = mQuestionList!![mCurrentPosition - 1]

            // Запоминаем ответ студента
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
                    isCorrect = (
                            mSelectedOptionPosition == question.correctAnswer
                            )
                )
            )

            // Подсчитываем правильные ответы
            if (mSelectedOptionPosition == question.correctAnswer) {
                mCorrectAnswers++
            }

            isAnswerChecked = true
            showAnswerFeedback()
            updateCheckedButtonText()
        }

        // Выход из тестирования
        fabHome.setOnClickListener {
            onBackPressed()
        }
    }

    // Сохранение состояния при пересоздании Activity
    override fun onSaveInstanceState(outState: Bundle) {

        outState.putString(
            "result_document_id",
            resultDocument.id
        )

        outState.putInt(
            "current_position",
            mCurrentPosition
        )

        outState.putInt(
            "correct_answers",
            mCorrectAnswers
        )

        outState.putInt(
            "selected_option",
            mSelectedOptionPosition
        )

        outState.putBoolean(
            "test_completed",
            isTestCompleted
        )
        outState.putBoolean("answer_checked", isAnswerChecked)

        outState.putParcelableArrayList(
            "student_answers",
            ArrayList(studentAnswers)
        )

        super.onSaveInstanceState(outState)
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
            StudentSession.getStudentName(this)
        )

        intent.putExtra(
            Constants.USER_GROUP,
            StudentSession.getStudentGroup(this)
        )

        startActivity(intent)
        finish()
    }

    // Сохранение результата теста №3
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

        try {

            // Подготавливаем ответы студента
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

            // Формируем локальный результат теста №3
            val localResult = JSONObject().apply {

                put(
                    "studentName",
                    StudentSession.getStudentName(
                        this@SelfControlTheoryActivity
                    )
                )

                put(
                    "studentGroup",
                    StudentSession.getStudentGroup(
                        this@SelfControlTheoryActivity
                    )
                )

                put("testNumber", 3)
                put("correctAnswers", mCorrectAnswers)
                put("totalQuestions", mQuestionList!!.size)
                put("answers", JSONArray(answers))
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
                "После локального сохранения теста №3: " +
                        PendingResultStorage.getResults(
                            applicationContext
                        ).size
            )

            // Отправкой в Firestore занимается только
            // PendingResultSync
            PendingResultSync.sync(applicationContext)

            // Завершаем тест после локального сохранения,
            // не ожидая ответа Firestore
            isSaving = false

            Toast.makeText(
                this,
                "Результат сохранён на устройстве",
                Toast.LENGTH_SHORT
            ).show()

            onSuccess()

        } catch (exception: Exception) {

            isSaving = false
            btnSubmit.isEnabled = true
            btnSubmit.text = "Повторить сохранение"

            Log.e(
                "PENDING_RESULTS",
                "Ошибка локального сохранения теста №3",
                exception
            )

            Toast.makeText(
                this,
                "Не удалось сохранить результат теста",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Отображение текущего вопроса
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

    // Сброс оформления вариантов ответа
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

            option.background = ContextCompat.getDrawable(
                this,
                R.drawable.default_option_border_bg
            )
        }
    }

    // Выделение выбранного варианта
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

        tv.background = ContextCompat.getDrawable(
            this,
            R.drawable.selected_option_border_bg
        )
    }

    private fun updateCheckedButtonText() {
        btnSubmit.text = if (mCurrentPosition == mQuestionList!!.size) {
            "Завершить тест"
        } else {
            "Следующий вопрос"
        }
        btnSubmit.isEnabled = true
    }

    private fun showAnswerFeedback() {
        val question = mQuestionList!![mCurrentPosition - 1]
        val options = listOf(tvOptionOne, tvOptionTwo, tvOptionThree)
        options.forEachIndexed { index, option ->
            val number = index + 1
            val drawable = when {
                number == question.correctAnswer -> R.drawable.correct_option_border_bg
                number == mSelectedOptionPosition -> R.drawable.wrong_option_border_bg
                else -> R.drawable.default_option_border_bg
            }
            option.background = ContextCompat.getDrawable(this, drawable)
            option.setTextColor(
                if (number == question.correctAnswer || number == mSelectedOptionPosition) Color.BLACK else Color.parseColor(
                    "#7A8089"
                )
            )
            option.setTypeface(
                Typeface.DEFAULT,
                if (number == question.correctAnswer || number == mSelectedOptionPosition) Typeface.BOLD else Typeface.NORMAL
            )
        }
    }

    // Подтверждение выхода из тестирования
    override fun onBackPressed() {

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.txt_confirm))
            .setMessage(getString(R.string.txt_exit_question))
            .setPositiveButton(
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
            .setNegativeButton(
                getString(R.string.exit_no),
                null
            )
            .show()
    }
}
