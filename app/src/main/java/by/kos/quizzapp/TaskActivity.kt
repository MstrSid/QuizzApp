
package by.kos.quizzapp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.json.JSONObject

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

    private lateinit var resultDocument: DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firestore = FirebaseFirestore.getInstance()

        val resultId = savedInstanceState
            ?.getString("result_document_id")
            ?: firestore.collection("test_results")
                .document()
                .id

        resultDocument = firestore
            .collection("test_results")
            .document(resultId)

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

        // Восстанавливаем состояние тестирования
        if (savedInstanceState != null) {

            mCurrentPosition = savedInstanceState.getInt(
                "current_position", 1
            )

            mCorrectAnswers = savedInstanceState.getInt(
                "correct_answers", 0
            )

            isTestCompleted = savedInstanceState.getBoolean(
                "test_completed", false
            )

            val selectedOptions = savedInstanceState
                .getIntegerArrayList("selected_options")

            mSelectedOptionPosition.clear()
            mSelectedOptionPosition.addAll(
                selectedOptions ?: emptyList()
            )

            // Восстанавливаем предыдущие ответы
            val savedAnswersJson = savedInstanceState
                .getString("student_answers_json")

            if (savedAnswersJson != null) {
                restoreStudentAnswers(savedAnswersJson)
            }
        }

        // Обработчики выбора вариантов
        setupCheckBox(chOptionOne, 1)
        setupCheckBox(chOptionTwo, 2)
        setupCheckBox(chOptionThree, 3)
        setupCheckBox(chOptionFour, 4)

        if (isTestCompleted) {

            showCompletedState()

        } else {

            // Сохраняем выбранные варианты до setQuestion()
            val selectedOptions =
                mSelectedOptionPosition.toSet()

            setQuestion()

            // Восстанавливаем выбранные варианты
            restoreSelectedOptions(selectedOptions)
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

            if (mSelectedOptionPosition.isEmpty()) {
                return@setOnClickListener
            }

            val question = mQuestionList!![mCurrentPosition - 1]

            val selectedAnswers =
                mSelectedOptionPosition.sorted()

            val correctAnswers =
                question.correctOptions.sorted()

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

                showCompletedState()

                saveTestResult {
                    openResultActivity()
                }
            }
        }

        fabHome.setOnClickListener {
            onBackPressed()
        }
    }

    // Сохранение состояния Activity
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

        outState.putBoolean(
            "test_completed",
            isTestCompleted
        )

        outState.putIntegerArrayList(
            "selected_options",
            ArrayList(mSelectedOptionPosition)
        )

        outState.putString(
            "student_answers_json",
            serializeStudentAnswers()
        )

        super.onSaveInstanceState(outState)
    }

    // Преобразуем ответы в JSON для Bundle
    private fun serializeStudentAnswers(): String {

        val jsonArray = JSONArray()

        studentAnswers.forEach { answer ->

            val jsonObject = JSONObject()

            jsonObject.put(
                "questionId",
                answer["questionId"]
            )

            jsonObject.put(
                "questionText",
                answer["questionText"]
            )

            jsonObject.put(
                "options",
                JSONArray(answer["options"] as List<*>)
            )

            jsonObject.put(
                "selectedAnswers",
                JSONArray(answer["selectedAnswers"] as List<*>)
            )

            jsonObject.put(
                "correctAnswers",
                JSONArray(answer["correctAnswers"] as List<*>)
            )

            jsonObject.put(
                "isCorrect",
                answer["isCorrect"]
            )

            jsonArray.put(jsonObject)
        }

        return jsonArray.toString()
    }

    // Восстанавливаем ответы из JSON
    private fun restoreStudentAnswers(json: String) {

        studentAnswers.clear()

        val jsonArray = JSONArray(json)

        for (i in 0 until jsonArray.length()) {

            val item = jsonArray.getJSONObject(i)

            val optionsArray = item.getJSONArray("options")
            val selectedArray = item.getJSONArray("selectedAnswers")
            val correctArray = item.getJSONArray("correctAnswers")

            val options = List(optionsArray.length()) { index ->
                optionsArray.getString(index)
            }

            val selectedAnswers =
                List(selectedArray.length()) { index ->
                    selectedArray.getInt(index)
                }

            val correctAnswers =
                List(correctArray.length()) { index ->
                    correctArray.getInt(index)
                }

            studentAnswers.add(
                mapOf(
                    "questionId" to item.getInt("questionId"),
                    "questionText" to item.getString("questionText"),
                    "options" to options,
                    "selectedAnswers" to selectedAnswers,
                    "correctAnswers" to correctAnswers,
                    "isCorrect" to item.getBoolean("isCorrect")
                )
            )
        }
    }

    // Восстанавливаем отмеченные CheckBox
    private fun restoreSelectedOptions(
        selectedOptions: Set<Int>
    ) {

        val checkBoxes = listOf(
            chOptionOne,
            chOptionTwo,
            chOptionThree,
            chOptionFour
        )

        mSelectedOptionPosition.clear()

        checkBoxes.forEachIndexed { index, checkBox ->

            val optionNumber = index + 1
            val isSelected = optionNumber in selectedOptions

            checkBox.isChecked = isSelected

            updateOptionAppearance(
                checkBox,
                isSelected
            )
        }

        mSelectedOptionPosition.clear()
        mSelectedOptionPosition.addAll(selectedOptions)

        btnSubmit.isEnabled =
            mSelectedOptionPosition.isNotEmpty()
    }

    // Состояние завершённого тестирования
    private fun showCompletedState() {

        progressBar.progress = progressBar.max
        tvProgress.text =
            "${progressBar.max}/${progressBar.max}"

        tvQuestion.text = "Тестирование завершено"

        ivImage.visibility = View.GONE

        val checkBoxes = listOf(
            chOptionOne,
            chOptionTwo,
            chOptionThree,
            chOptionFour
        )

        checkBoxes.forEach { checkBox ->
            checkBox.isChecked = false
            checkBox.visibility = View.GONE
        }

        btnSubmit.text = "Повторить сохранение"
        btnSubmit.isEnabled = true
    }

    // Настройка CheckBox
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

            updateOptionAppearance(
                checkBox,
                isChecked
            )

            btnSubmit.isEnabled =
                mSelectedOptionPosition.isNotEmpty()
        }
    }

    // Оформление выбранного варианта
    private fun updateOptionAppearance(
        checkBox: CheckBox,
        isSelected: Boolean
    ) {

        checkBox.setTextColor(
            Color.parseColor("#7A8089")
        )

        if (isSelected) {

            checkBox.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
            )

            checkBox.background =
                ContextCompat.getDrawable(
                    this,
                    R.drawable.selected_option_border_bg
                )

        } else {

            checkBox.typeface = Typeface.DEFAULT

            checkBox.background =
                ContextCompat.getDrawable(
                    this,
                    R.drawable.default_option_border_bg
                )
        }
    }

    // Отображение вопроса
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
        tvProgress.text =
            "$mCurrentPosition/${progressBar.max}"

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

    // Переход к результатам
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

    // Сохранение результата теста №4
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

            // Создаём копию ответов для сохранения
            val answers = studentAnswers.toList()

            // Формируем локальный результат теста №4
            val localResult = JSONObject().apply {

                put(
                    "studentName",
                    StudentSession.getStudentName(
                        this@TaskActivity
                    )
                )

                put(
                    "studentGroup",
                    StudentSession.getStudentGroup(
                        this@TaskActivity
                    )
                )

                put("testNumber", 4)
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
                "После локального сохранения теста №4: " +
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
                "Ошибка локального сохранения теста №4",
                exception
            )

            Toast.makeText(
                this,
                "Не удалось сохранить результат теста",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Подтверждение выхода
    override fun onBackPressed() {

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.txt_confirm))
            .setMessage(getString(R.string.txt_exit_question))
            .setPositiveButton(
                getString(R.string.exit_yes)
            ) { _, _ ->

                startActivity(
                    Intent(this, MainActivity::class.java)
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
