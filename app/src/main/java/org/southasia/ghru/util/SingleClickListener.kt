package org.southasia.ghru.util

import android.view.View
import android.view.ViewConfiguration

class SingleClickListener(private val click: (v: View) -> Unit) : View.OnClickListener {

    companion object {
        private val DOUBLE_CLICK_TIMEOUT = ViewConfiguration.getDoubleTapTimeout()
    }

    private var lastClick: Long = 0

    override fun onClick(v: View) {
        if (getLastClickTimeout() > DOUBLE_CLICK_TIMEOUT) {
            lastClick = System.currentTimeMillis()
            click(v)
        }
    }

    private fun getLastClickTimeout(): Long {
        return System.currentTimeMillis() - lastClick
    }
}