package by.kos.quizzapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import by.kos.quizzapp.databinding.FragmentMiniTestsBinding


class MiniTestsFragment : Fragment() {

    private var _binding: FragmentMiniTestsBinding? = null
    private val binding get() = _binding!!

    private lateinit var btnTheory: Button
    private lateinit var btnTasks: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMiniTestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnTheory = binding.btnStartTheory
        btnTasks = binding.btnStartTasks

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity?.window?.setDecorFitsSystemWindows(false)
        } else {
            activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        btnTheory.setOnClickListener {

            val intent = Intent(activity, SelfControlTheoryActivity::class.java)
            intent.putExtra(Constants.USER_NAME, "")
            Constants.TEST_NUMBER = 3
            startActivity(intent)
            activity?.finish()
        }

        /*btnTasks.setOnClickListener {
            val intent = Intent(activity, QuizQuestionsActivity::class.java)
            intent.putExtra(Constants.USER_NAME, "")
            Constants.TEST_NUMBER = 2
            startActivity(intent)
            activity?.finish()

        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}