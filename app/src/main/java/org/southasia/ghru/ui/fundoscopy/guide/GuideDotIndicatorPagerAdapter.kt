package org.southasia.ghru.ui.fundoscopy.guide

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import org.southasia.ghru.R

class GuideDotIndicatorPagerAdapter : PagerAdapter() {


    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val item = LayoutInflater.from(container.context)
            .inflate(R.layout.material_page, container, false)
        //CardView cardView = item.findViewById(R.id.card_view);
        //cardView.setCardBackgroundColor(
        //    ContextCompat.getColor(container.getContext(), (items.get(position).color)));
        container.addView(item)
        return item
    }

    override fun getCount(): Int {
        return 4
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    private class Item private constructor(private val color: Int)
}