// Copyright 2020, Collabora, Ltd.
// SPDX-License-Identifier: BSL-1.0
package org.khronos.openxr.runtime_broker

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.khronos.openxr.broker_lib.OpenXRLoaderUtils
import org.khronos.openxr.runtime_broker.data.RuntimeRepository
import org.khronos.openxr.runtime_broker.utils.RuntimeChooser
import org.khronos.openxr.runtime_broker.utils.RuntimeData

class InstallableRuntimeChooser : RuntimeChooser {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface RepositoryEntryPoint {
        fun runtimeRepository(): RuntimeRepository
    }

    /**
     * Get the currently active runtime for a given specification major version.
     *
     * @param context      a Context to use when searching for runtimes.
     * @param majorVersion a major version number of OpenXR.
     * @return the active runtime, or null if something went wrong or none were found.
     */
    override fun getActiveRuntime(context: Context, majorVersion: Int, abi: String): RuntimeData? {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            RepositoryEntryPoint::class.java
        )

        val selected = entryPoint.runtimeRepository().getSelectedRuntime()
        return OpenXRLoaderUtils.findOpenXRRuntimes(context, majorVersion, abi)?.find {
            it.packageName == selected
        }
    }
}
