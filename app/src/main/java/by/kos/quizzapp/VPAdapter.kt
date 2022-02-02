package by.kos.quizzapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class VPAdapter(fm: FragmentManager, behavior: Int) : FragmentPagerAdapter(fm, behavior) {

    private val fragmentsList = ArrayList<Fragment>()
    private val fragmentsTitle = ArrayList<String>()

    override fun getCount(): Int {
       return fragmentsList.size
    }

    override fun getItem(position: Int): Fragment {
        return fragmentsList[position]
    }

    public fun addFragment(fm: Fragment, title: String){
        fragmentsList.add(fm)
        fragmentsTitle.add(title)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragmentsTitle[position]
    }
}