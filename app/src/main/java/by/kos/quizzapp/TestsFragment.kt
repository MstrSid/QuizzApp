package by.kos.quizzapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import by.kos.quizzapp.databinding.FragmentTestsBinding


class TestsFragment : Fragment() {

    private var _binding: FragmentTestsBinding? = null
    private val binding get() = _binding!!

    private lateinit var btnStart: Button
    private lateinit var btnStart2: Button
    private lateinit var etName: EditText


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnStart = binding.btnStart
        btnStart2 = binding.btnStart2
        etName = binding.etName

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity?.window?.setDecorFitsSystemWindows(false)
        } else {
            activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        btnStart.setOnClickListener {
            if (etName.text.toString().trim().isEmpty()) {
                Toast.makeText(activity, "Введите имя, пожалуйста.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(activity, QuizQuestionsActivity::class.java)
                intent.putExtra(Constants.USER_NAME, etName.text.toString())
                Constants.TEST_NUMBER = 1
                startActivity(intent)
                activity?.finish()
            }
        }

        btnStart2.setOnClickListener {
            if (etName.text.toString().trim().isEmpty()) {
                Toast.makeText(activity, "Введите имя, пожалуйста.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(activity, QuizQuestionsActivity::class.java)
                intent.putExtra(Constants.USER_NAME, etName.text.toString())
                Constants.TEST_NUMBER = 2
                startActivity(intent)
                activity?.finish()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}