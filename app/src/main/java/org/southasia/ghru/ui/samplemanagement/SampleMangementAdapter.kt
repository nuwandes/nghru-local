package org.southasia.ghru.ui.samplemanagement

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.R
import org.southasia.ghru.databinding.NghruItemBinding
import org.southasia.ghru.ui.common.DataBoundListAdapter
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.HomeItem


class SampleMangementAdapter(
    private val dataBindingComponent: DataBindingComponent,
    appExecutors: AppExecutors,
    private val callback: ((HomeItem) -> Unit)?
) : DataBoundListAdapter<HomeItem, NghruItemBinding>(
    appExecutors = appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<HomeItem>() {
        override fun areItemsTheSame(oldItem: HomeItem, newItem: HomeItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HomeItem, newItem: HomeItem): Boolean {
            return oldItem.id == newItem.id
                    && oldItem.name == newItem.name
        }
    }
) {

    override fun createBinding(parent: ViewGroup): NghruItemBinding {
        val binding = DataBindingUtil
            .inflate<NghruItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.nghru_item,
                parent,
                false,
                dataBindingComponent
            )
        binding.root.singleClick {
            binding.homeItem?.let {
                callback?.invoke(it)
            }
        }
        return binding
    }

    override fun bind(binding: NghruItemBinding, item: HomeItem) {
        binding.homeItem = item
    }
}
