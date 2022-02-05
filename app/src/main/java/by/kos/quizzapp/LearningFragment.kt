package by.kos.quizzapp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import by.kos.quizzapp.databinding.FragmentLearningBinding


class LearningFragment : Fragment() {

    private var _binding: FragmentLearningBinding? = null
    private val binding get() = _binding!!
    private lateinit var ivJack: ImageView
    private lateinit var ivFactory: ImageView
    private lateinit var btnConspect: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ivJack = binding.ivJack
        ivFactory = binding.ivFactory
        btnConspect = binding.btnStartConspect

        ivJack.setOnClickListener {
            val intent = Intent(activity, LearnMaterialsActivity::class.java)
            intent.putExtra("videoName", R.raw.jack)
            startActivity(intent)
            //activity?.finish()
        }

        ivFactory.setOnClickListener {
            val intent = Intent(activity, LearnMaterialsActivity::class.java)
            intent.putExtra("videoName", R.raw.factory)
            startActivity(intent)
            //activity?.finish()
        }

        btnConspect.setOnClickListener {
            val intent = Intent(activity, PdfActivity::class.java)
            //intent.putExtra("filePdf", R.raw.pocket)
            startActivity(intent)
            activity?.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}