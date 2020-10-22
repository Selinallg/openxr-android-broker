// Copyright 2020-2021, Collabora, Ltd.
// SPDX-License-Identifier: BSL-1.0

package org.khronos.openxr.system_runtime_broker

import android.content.Context
import org.khronos.openxr.runtime_broker.utils.RuntimeChooser
import org.khronos.openxr.runtime_broker.utils.RuntimeData

class SystemRuntimeChooser : RuntimeChooser {
    /**
     * Get the currently active runtime for a given specification major version.
     *
     *
     * The first one of these is the preferred/"active" runtime
     *
     * @param context      a Context to use when searching for runtimes.
     * @param majorVersion a major version number of OpenXR.
     * @param abi          the ABI to return data for.
     * @return the active runtime, or null if something went wrong or none were found.
     */
    override fun getActiveRuntime(context: Context, majorVersion: Int, abi: String): RuntimeData? {
        TODO("Not yet implemented")
    }
}