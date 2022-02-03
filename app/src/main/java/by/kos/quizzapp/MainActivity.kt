package by.kos.quizzapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import by.kos.quizzapp.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tabLayout = binding.tblMain
        viewPager = binding.vPager

        val adapter = VPAdapter(this)

        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Тесты"
                    tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_test_24)
                }
                1 -> {
                    tab.text = "Закрепление"
                    tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_check_24)
                }
                2 -> {
                    tab.text = "Учебное"
                    tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_book_24)
                }

            }
        }.attach()
    }
}