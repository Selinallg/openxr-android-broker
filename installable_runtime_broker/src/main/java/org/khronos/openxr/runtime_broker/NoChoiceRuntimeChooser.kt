// Copyright 2020, Collabora, Ltd.
// SPDX-License-Identifier: BSL-1.0
package org.khronos.openxr.runtime_broker

import android.content.Context
import android.util.Log
import org.khronos.openxr.broker_lib.OpenXRLoaderUtils
import org.khronos.openxr.runtime_broker.utils.RuntimeChooser
import org.khronos.openxr.runtime_broker.utils.RuntimeData

class NoChoiceRuntimeChooser : RuntimeChooser {

    /**
     * Get the currently active runtime for a given specification major version.
     *
     *
     * The first one of these is the preferred/"active" runtime
     *
     * @param context      a Context to use when searching for runtimes.
     * @param majorVersion a major version number of OpenXR.
     * @return the active runtime, or null if something went wrong or none were found.
     */
    override fun getActiveRuntime(context: Context, majorVersion: Int, abi: String): RuntimeData? {
        val runtimes = OpenXRLoaderUtils.findOpenXRRuntimes(context, majorVersion, abi)
        if (runtimes != null && runtimes.size > 1) {
            Log.w("RuntimeBroker", "Got more than one runtime, can't decide!")
        }
        // we have no way of choosing if there's more than one, so only return one if there's only one.
        return if (runtimes != null && runtimes.size == 1) {
            runtimes[0]
        } else null
    }
}