// Copyright 2021, Qualcomm Innovation Center, Inc.
// SPDX-License-Identifier: BSL-1.0
package org.khronos.openxr.runtime_broker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.khronos.openxr.runtime_broker.data.RuntimeInformation
import org.khronos.openxr.runtime_broker.databinding.ListitemTwoLineBinding

class InstalledRuntimeAdapter :
    RuntimeListAdapter<RuntimeInformation, InstalledRuntimeAdapter.TwoLineViewHolder>(
        RuntimeInformationDiffCallback()
    ) {

    private class RuntimeInformationDiffCallback : DiffUtil.ItemCallback<RuntimeInformation>() {
        override fun areItemsTheSame(
            oldItem: RuntimeInformation,
            newItem: RuntimeInformation
        ): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(
            oldItem: RuntimeInformation,
            newItem: RuntimeInformation
        ): Boolean {
            return oldItem.applicationName == newItem.applicationName
                    && oldItem.majorVersion == newItem.majorVersion
                    && oldItem.selected == newItem.selected
        }
    }

    class TwoLineViewHolder(
        private val binding: ListitemTwoLineBinding,
        private var itemClickListener: OnItemClickListener<RuntimeInformation>? = null
    ) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                itemClickListener?.invoke(
                    binding.runtime!!,
                    adapterPosition
                )
            }
        }

        fun bind(item: RuntimeInformation) {
            binding.apply {
                runtime = item
                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TwoLineViewHolder {
        return TwoLineViewHolder(
            ListitemTwoLineBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), itemClickListener
        )
    }

    override fun onBindViewHolder(holder: TwoLineViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

}