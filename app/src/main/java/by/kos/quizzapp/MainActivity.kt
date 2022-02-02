package by.kos.quizzapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import by.kos.quizzapp.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var btnLearn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tabLayout = binding.tblMain
        viewPager = binding.vPager
        tabLayout.setupWithViewPager(viewPager)

        val adapter = VPAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        )
        adapter.addFragment(TestsFragment(), "Тесты")
        adapter.addFragment(MiniTestsFragment(), "Закрепление")
        adapter.addFragment(LearningFragment(), "Учебное")

        viewPager.adapter = adapter

        btnLearn = findViewById(R.id.btn_learn)

        btnLearn.setOnClickListener {
            val intent = Intent(this, LearnMaterialsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}