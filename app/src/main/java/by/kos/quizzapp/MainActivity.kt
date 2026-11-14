
package by.kos.quizzapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.viewpager2.widget.ViewPager2
import by.kos.quizzapp.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var fabInfo: FloatingActionButton

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase Anonymous Authentication
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            Log.d(
                "FirebaseTest",
                "Уже авторизован. UID: ${currentUser.uid}"
            )
        } else {
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(
                            "FirebaseTest",
                            "Анонимная авторизация успешна. UID: ${auth.currentUser?.uid}"
                        )
                    } else {
                        Log.e(
                            "FirebaseTest",
                            "Ошибка авторизации",
                            task.exception
                        )
                    }
                }
        }

        tabLayout = binding.tblMain
        viewPager = binding.vPager
        fabInfo = binding.fabInfo

        // Информация о приложении
        fabInfo.setOnClickListener {
            val builder = AlertDialog.Builder(this)

            builder.setTitle(getString(R.string.txt_about))

            builder.setMessage(
                HtmlCompat.fromHtml(
                    getString(R.string.alert_info_text),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            )

            builder.setIcon(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.emblem
                )
            )

            builder.setNeutralButton(
                getString(R.string.txt_close)
            ) { _, _ -> }

            builder.show()
        }

        fabInfo.setOnLongClickListener {
            showStudentDialog()
            true
        }

        // Настройка вкладок
        val adapter = VPAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Тесты"
                    tab.icon = ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_test_24
                    )
                }

                1 -> {
                    tab.text = "Закрепление"
                    tab.icon = ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_check_24
                    )
                }

                2 -> {
                    tab.text = "Учебное"
                    tab.icon = ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_book_24
                    )
                }
            }
        }.attach()

        // При первом запуске запрашиваем данные студента
        if (!StudentSession.hasStudent(this)) {
            showStudentDialog()
        }
    }

    // Окно ввода данных студента
    private fun showStudentDialog() {

        val density = resources.displayMetrics.density
        val padding = (20 * density).toInt()
        val spacing = (12 * density).toInt()

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, spacing, padding, 0)
        }

        val etName = EditText(this).apply {
            hint = "ФИО студента"
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_CAP_WORDS
            setSingleLine(true)
            filters = arrayOf(
                android.text.InputFilter.LengthFilter(150)
            )
        }

        val etGroup = EditText(this).apply {
            hint = "Учебная группа"
            inputType = InputType.TYPE_CLASS_TEXT
            setSingleLine(true)
            filters = arrayOf(
                android.text.InputFilter.LengthFilter(50)
            )
        }

        layout.addView(
            etName,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        layout.addView(
            etGroup,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val dialog = AlertDialog.Builder(this)
            .setTitle("Данные студента")
            .setMessage(
                "Введите ФИО и учебную группу для прохождения тестирования."
            )
            .setView(layout)
            .setPositiveButton("Сохранить", null)
            .create()

        // Нельзя закрыть окно без заполнения данных
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        dialog.setOnShowListener {
            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener {

                val name = etName.text.toString().trim()
                val group = etGroup.text.toString().trim()

                when {
                    name.isBlank() -> {
                        etName.error = "Введите ФИО"
                        etName.requestFocus()
                    }

                    group.isBlank() -> {
                        etGroup.error = "Введите учебную группу"
                        etGroup.requestFocus()
                    }

                    else -> {
                        StudentSession.saveStudent(
                            this,
                            name,
                            group
                        )

                        Toast.makeText(
                            this,
                            "Данные студента сохранены",
                            Toast.LENGTH_SHORT
                        ).show()

                        dialog.dismiss()
                    }
                }
            }
        }

        dialog.show()
    }

    // Подтверждение выхода из приложения
    override fun onBackPressed() {

        val builder = AlertDialog.Builder(this)

        builder.setTitle(
            getString(R.string.txt_confirm)
        )

        builder.setMessage(
            getString(R.string.txt_exit_another)
        )

        builder.setPositiveButton(
            getString(R.string.exit_yes)
        ) { _, _ ->
            finish()
        }

        builder.setNegativeButton(
            getString(R.string.exit_no)
        ) { _, _ -> }

        builder.show()
    }
}
