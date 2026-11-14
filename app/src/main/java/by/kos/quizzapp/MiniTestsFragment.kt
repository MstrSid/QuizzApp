
package by.kos.quizzapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import by.kos.quizzapp.databinding.FragmentMiniTestsBinding

class MiniTestsFragment : Fragment() {

    private var _binding: FragmentMiniTestsBinding? = null
    private val binding get() = _binding!!

    private lateinit var btnTheory: Button
    private lateinit var btnTasks: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMiniTestsBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        btnTheory = binding.btnStartTheory
        btnTasks = binding.btnStartTasks

        // Теоретические вопросы
        btnTheory.setOnClickListener {

            val intent = Intent(
                requireContext(),
                SelfControlTheoryActivity::class.java
            )

            intent.putExtra(
                Constants.USER_NAME,
                StudentSession.getStudentName(requireContext())
            )

            intent.putExtra(
                Constants.USER_GROUP,
                StudentSession.getStudentGroup(requireContext())
            )

            Constants.TEST_NUMBER = 3

            startActivity(intent)
            activity?.finish()
        }

        // Практические задания
        btnTasks.setOnClickListener {

            val intent = Intent(
                requireContext(),
                TaskActivity::class.java
            )

            intent.putExtra(
                Constants.USER_NAME,
                StudentSession.getStudentName(requireContext())
            )

            intent.putExtra(
                Constants.USER_GROUP,
                StudentSession.getStudentGroup(requireContext())
            )

            Constants.TASK_NUMBER = 1

            startActivity(intent)
            activity?.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
