package org.southasia.ghru.ui.fundoscopy.reading

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.R
import org.southasia.ghru.databinding.FundoscopyAssetItemBinding
import org.southasia.ghru.ui.common.DataBoundListAdapter
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.Asset


class AssetAdapter(
    private val dataBindingComponent: DataBindingComponent,
    appExecutors: AppExecutors,
    private val callback: ((Asset) -> Unit)?
) : DataBoundListAdapter<Asset, FundoscopyAssetItemBinding>(
    appExecutors = appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<Asset>() {
        override fun areItemsTheSame(oldItem: Asset, newItem: Asset): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Asset, newItem: Asset): Boolean {
            return oldItem.id == newItem.id
                    && oldItem.url == newItem.url
        }
    }
) {

    override fun createBinding(parent: ViewGroup): FundoscopyAssetItemBinding {
        val binding = DataBindingUtil
            .inflate<FundoscopyAssetItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.fundoscopy_asset_item,
                parent,
                false,
                dataBindingComponent
            )
        binding.root.singleClick {
            binding.asset?.let {
                callback?.invoke(it)
            }
        }
        return binding
    }

    override fun bind(binding: FundoscopyAssetItemBinding, item: Asset) {
        binding.asset = item
    }
}
