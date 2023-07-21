package org.southasia.ghru.ui.registerpatient_sg.scanqrcode.membersdialog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.R
import org.southasia.ghru.databinding.MemberDialogItemBinding
import org.southasia.ghru.ui.common.DataBoundListAdapter
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.request.Member


class MembersDialogAdapterSG(
    private val dataBindingComponent: DataBindingComponent,
    appExecutors: AppExecutors,
    private val callback: ((Member) -> Unit)?
) : DataBoundListAdapter<Member, MemberDialogItemBinding>(
    appExecutors = appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<Member>() {
        override fun areItemsTheSame(oldItem: Member, newItem: Member): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Member, newItem: Member): Boolean {
            return oldItem.id == newItem.id
                    && oldItem.timestamp == newItem.timestamp
        }
    }
) {

    override fun createBinding(parent: ViewGroup): MemberDialogItemBinding {
        val binding = DataBindingUtil
            .inflate<MemberDialogItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.member_dialog_item,
                parent,
                false,
                dataBindingComponent
            )
        binding.root.singleClick {
            binding.member?.let {
                callback?.invoke(it)
            }
        }
        return binding
    }

    override fun bind(binding: MemberDialogItemBinding, item: Member) {
        binding.member = item
    }
}
