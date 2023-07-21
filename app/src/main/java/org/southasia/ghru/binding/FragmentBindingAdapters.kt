package org.southasia.ghru.binding


import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import javax.inject.Inject

/**
 * Binding adapters that work with a fragment instance.
 */

class FragmentBindingAdapters @Inject constructor(val fragment: Fragment) {
    @BindingAdapter("imageUrl")
    fun bindImage(imageView: ImageView, url: String?) {
        Glide.with(fragment).load(url).into(imageView)
    }

    @BindingAdapter("image")
    fun bindIcon(imageView: ImageView, url: Int) {
        //imageView.setBackgroundResource(url);
        //imageView.setImageDrawable(fragment.getActivity().getDrawable(url));
        imageView.setImageResource(url)
    }
}
