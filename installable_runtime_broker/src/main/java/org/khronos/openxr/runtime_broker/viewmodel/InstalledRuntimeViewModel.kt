// Copyright 2021, Qualcomm Innovation Center, Inc.
// SPDX-License-Identifier: BSL-1.0
package org.khronos.openxr.runtime_broker.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.khronos.openxr.runtime_broker.data.RuntimeInformation
import org.khronos.openxr.runtime_broker.data.RuntimeRepository
import javax.inject.Inject

@HiltViewModel
class InstalledRuntimeViewModel @Inject constructor(private val runtimeRepository: RuntimeRepository) :
    ViewModel() {
    val runtimes: MutableLiveData<List<RuntimeInformation>> = MutableLiveData()

    fun load() {
        runtimes.value = runtimeRepository.getInstalledRuntimes()
    }

    fun updateSelection(runtimeInformation: RuntimeInformation) {
        runtimeRepository.updateRuntimeSelection(
            runtimeInformation.packageName,
            runtimeInformation.selected
        )
        load()
    }
}
