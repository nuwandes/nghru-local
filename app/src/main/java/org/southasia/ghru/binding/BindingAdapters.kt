package org.southasia.ghru.binding

import android.annotation.SuppressLint
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import org.southasia.ghru.R
import org.southasia.ghru.util.collapse
import org.southasia.ghru.util.expand
import org.southasia.ghru.vo.Date
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*


/**
 * Data Binding adapters specific to the app.
 */
object BindingAdapters {
    @JvmStatic
    @BindingAdapter("visibleGone")
    fun showHide(view: View, show: Boolean) {
        //println("show" + show)
        view.visibility = if (show) {
            View.VISIBLE
        } else {
            View.GONE
        }
        if (show) {
            view.expand()
        } else {
            view.collapse()
        }
    }

    @JvmStatic
    @BindingAdapter("visibilityEmpty")
    fun showvisibilityEmpty(view: View, show: Boolean) {
        //println("show" + show)
        view.visibility = if (show) View.GONE else View.VISIBLE
    }

    @JvmStatic
    @BindingAdapter("visibilityEmptyOnString")
    fun showVisibilityEmptyOnString(view: View, show: String?) {
        //println("show" + show)
        view.visibility = if (show.isNullOrEmpty()) View.GONE else View.VISIBLE
    }


    @JvmStatic
    @BindingAdapter("datefromTimeStamp")
    fun showDatefromTimeStamp(view: TextView, timestamp: Long) {
        //println("show" + show)
        val stamp = Timestamp(timestamp)
        val date = Date(stamp.getTime())
        try {
            val sdf = SimpleDateFormat("dd/mm/yyyy hh:mm", Locale.ENGLISH)
            view.setText(sdf.format(date))
        } catch (e: Exception) {
            //Crashlytics.logException(e)
        }
    }


    @JvmStatic
    @BindingAdapter("visibleAnim")
    fun fadeInAnimation(view: View, show: Boolean) {
        val slideDown: Animation = AnimationUtils.loadAnimation(view.context, R.anim.fade_in)
        val slideUp: Animation = AnimationUtils.loadAnimation(view.context, R.anim.fade_out)
        if (show) {
            view.startAnimation(slideDown)
        } else {
            view.startAnimation(slideUp)
        }

    }


    @JvmStatic
    @BindingAdapter("viewGoneAnim")
    fun fadeOutAnimation(view: View, show: Boolean) {
        val slideDown: Animation = AnimationUtils.loadAnimation(view.context, R.anim.fade_out)
        val slideUp: Animation = AnimationUtils.loadAnimation(view.context, R.anim.fade_in)
        if (show) {
            view.startAnimation(slideDown)
        } else {
            view.startAnimation(slideUp)
        }

    }

    @JvmStatic
    @BindingAdapter("expanCollapse")
    fun expanCollapse(view: ImageView, show: Boolean) {
        if (show) {
            view.setImageDrawable(view.context.getDrawable(R.drawable.ic_icon_chevron_up))
        } else {
            view.setImageDrawable(view.context.getDrawable(R.drawable.ic_icon_chevron_up_down))
        }

    }

    @JvmStatic
    @BindingAdapter("sync")
    fun sync(view: ImageView, show: Boolean) {
        if (show) {
            view.setImageDrawable(view.context.getDrawable(R.drawable.ic_sync_red))
        } else {
            view.setImageDrawable(view.context.getDrawable(R.drawable.ic_sync_green))
        }

    }

    @JvmStatic
    @BindingAdapter("syncStatus")
    fun syncStatus(view: ImageView, show: Boolean) {
        if (show) {
            view.setImageDrawable(view.context.getDrawable(R.drawable.ic_icon_status_info))
        } else {
            view.setImageDrawable(view.context.getDrawable(R.drawable.ic_icon_status_tick))
        }

    }


    @JvmStatic
    @BindingAdapter("id")
    fun toString(view: TextView, value: Long) {
        view.setText(value.toString())
    }

    @JvmStatic
    @BindingAdapter("int")
    fun toInt(view: TextView, value: Int) {
        view.setText(value.toString())
    }


    @SuppressLint("SimpleDateFormat")
    @JvmStatic
    @BindingAdapter("date")
    fun toString(view: TextView, value: Date) {
        // val date = SimpleDateFormat("ddMMyyyy", Locale.US).parse(value.day.toString() + value.month.toString() + value.year.toString())
        var c = Calendar.getInstance();
        c.set(value.year, value.month, value.day)
        val format = SimpleDateFormat("dd-MMM-yyyy", Locale.US)
        view.setText(format.format(c.time).toString())
    }
}
