
package by.kos.quizzapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import by.kos.quizzapp.databinding.FragmentTestsBinding

class TestsFragment : Fragment() {

    private var _binding: FragmentTestsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestsBinding.inflate(
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

        binding.btnStart.setOnClickListener {
            startTest(1)
        }

        binding.btnStart2.setOnClickListener {
            startTest(2)
        }
    }

    private fun startTest(testNumber: Int) {

        val studentName = StudentSession.getStudentName(requireContext())
        val studentGroup = StudentSession.getStudentGroup(requireContext())

        if (studentName.isBlank() || studentGroup.isBlank()) {
            Toast.makeText(
                requireContext(),
                "Данные студента не заполнены",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val intent = Intent(
            requireContext(),
            QuizQuestionsActivity::class.java
        )

        intent.putExtra(Constants.USER_NAME, studentName)
        intent.putExtra(Constants.USER_GROUP, studentGroup)

        Constants.TEST_NUMBER = testNumber

        startActivity(intent)
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
