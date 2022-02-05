package by.kos.quizzapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.viewpager2.widget.ViewPager2
import by.kos.quizzapp.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

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

        tabLayout = binding.tblMain
        viewPager = binding.vPager
        fabInfo = binding.fabInfo

        fabInfo.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.txt_about))
            builder.setMessage(HtmlCompat.fromHtml(getString(R.string.alert_info_text), HtmlCompat.FROM_HTML_MODE_LEGACY))
            builder.setIcon(getDrawable(R.drawable.emblem))

            builder.setNeutralButton(getString(R.string.txt_close)) { dialog, which ->
            }
            builder.show()
        }

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

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.txt_confirm))
        builder.setMessage(getString(R.string.txt_exit_another))

        builder.setPositiveButton(getString(R.string.exit_yes)) { dialog, which ->
            finish()
        }

        builder.setNegativeButton(getString(R.string.exit_no)) { dialog, which ->
        }
        builder.show()
    }
}