package org.southasia.ghru.util

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StyleRes
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*


/**
 * Click listener setter that prevents double click on the view it's set
 */
fun View.singleClick(l: (android.view.View?) -> Unit) {
    setOnClickListener(SingleClickListener(l))
}

fun View.getLocalTimeString(): String {
    val s = SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.US)
    return s.format(Date())
}

fun View.getLocalDateString(): String {
    val s = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return s.format(Date())
}


fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.shoKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, 0)
}

//
fun View.toSimpleDateString(date: Date): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return format.format(date)
}

private fun FragmentActivity.isNetworkAvailable(): Boolean {
    val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE)
    return if (connectivityManager is ConnectivityManager) {
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        networkInfo?.isConnected ?: false
    } else false
}

fun TextView.setTextAppearanceC(@StyleRes textAppearance: Int) = TextViewCompat.setTextAppearance(this, textAppearance)


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun TextView?.setDrawbleLeftColor(@StyleRes color: String) {
    val compoundDrawables = this?.compoundDrawables
    if (compoundDrawables != null) {
        val first = compoundDrawables.first()
        if (first != null) {
            first.setTint(Color.parseColor(color))
        } else {
            // compoundDrawables.last().setTint(Color.parseColor(color))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun Button.setDrawableRightColor(@StyleRes color: String) {
    val compoundDrawables = this.compoundDrawables
    if (compoundDrawables.get(2) != null) {
        compoundDrawables.get(2).setTint(Color.parseColor(color))
    }
}


fun View.expand() {
    val v = this
    v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    val targetHeight: Int = v.measuredHeight

    v.layoutParams.height = 1
    v.visibility = View.VISIBLE

    val a: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            v.layoutParams.height = if (interpolatedTime == 1f)
                LinearLayout.LayoutParams.WRAP_CONTENT
            else
                (targetHeight * interpolatedTime).toInt()
            v.requestLayout()

        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    a.duration = (targetHeight / v.context.resources.displayMetrics.density).toInt().toLong()
    v.startAnimation(a)
}

fun View.collapse() {
    val initialHeight: Int = this.measuredHeight
    val view = this
    val a: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            if (interpolatedTime == 1f) {
                view.visibility = View.GONE
            } else {
                view.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                view.requestLayout()
            }
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    a.duration = (initialHeight / view.context.resources.displayMetrics.density).toInt().toLong()
    view.startAnimation(a)
}


@Suppress("DEPRECATION")
fun String.fromHtml() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    Html.fromHtml(this, Html.FROM_HTML_MODE_COMPACT);
} else {
    Html.fromHtml(this)
}

inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)

//fun Snackbar.withTextColor(color: Int): Snackbar {
//    val tv = this.view.findViewById(android.support.design.R.id.snackbar_text) as TextView
//    tv.setTextColor(color)
//    return this
//}

fun Activity.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun MenuItem.setTitleColor(color: Int) {
    val hexColor = Integer.toHexString(color).toUpperCase().substring(2)
    val html = "<font color='#" + hexColor + "'>" + this.title.toString() + "</font>"
    if(html !=null) {
        this.title = html.parseAsHtml()
    }
}

fun String.parseAsHtml(): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this)
    }
}

