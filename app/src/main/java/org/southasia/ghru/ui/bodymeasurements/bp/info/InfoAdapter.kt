package org.southasia.ghru.ui.bodymeasurements.bp.info

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

// 1
class InfoAdapter(fragmentManager: FragmentManager) :
    FragmentStatePagerAdapter(fragmentManager) {

    // 2
    override fun getItem(position: Int): Fragment {
        return InfoFragment()
    }

    // 3
    override fun getCount(): Int {
        return 3
    }
}