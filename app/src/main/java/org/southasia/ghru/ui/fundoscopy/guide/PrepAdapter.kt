package org.southasia.ghru.ui.fundoscopy.guide

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

// 1
class PrepAdapter(fragmentManager: FragmentManager) :
    FragmentStatePagerAdapter(fragmentManager) {

    // 2
    override fun getItem(position: Int): Fragment {
        return PreperationFragment()
    }

    // 3
    override fun getCount(): Int {
        return 3
    }
}