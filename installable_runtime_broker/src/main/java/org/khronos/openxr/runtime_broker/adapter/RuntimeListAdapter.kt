// Copyright 2021, Qualcomm Innovation Center, Inc.
// SPDX-License-Identifier: BSL-1.0
package org.khronos.openxr.runtime_broker.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class RuntimeListAdapter<T, VH : RecyclerView.ViewHolder> (itemCallback: DiffUtil.ItemCallback<T>):
    ListAdapter<T, VH>(itemCallback) {

    protected var itemClickListener: OnItemClickListener<T>? = null

    fun setOnItemClickListener(listener: OnItemClickListener<T>) {
        itemClickListener = listener
    }
}

typealias OnItemClickListener<T> = (T, Int) -> Unit
