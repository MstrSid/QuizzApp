package by.kos.quizzapp

import androidx.fragment.app.*
import androidx.viewpager2.adapter.FragmentStateAdapter

class VPAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LearningFragment()
            1 -> TestsFragment()
            2 -> MiniTestsFragment()
            else -> LearningFragment()
        }
    }
}