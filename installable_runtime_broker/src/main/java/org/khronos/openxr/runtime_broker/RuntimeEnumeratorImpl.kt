// Copyright 2020-2021, Collabora, Ltd.
// SPDX-License-Identifier: BSL-1.0

package org.khronos.openxr.runtime_broker

import android.content.Context
import org.khronos.openxr.broker_lib.OpenXRLoaderUtils
import org.khronos.openxr.runtime_broker.utils.BrokerContract
import org.khronos.openxr.runtime_broker.utils.RuntimeData
import org.khronos.openxr.runtime_broker.utils.getRuntimeFromContentProvider

class RuntimeEnumeratorImpl : RuntimeEnumerator {
    /**
     * Get a list of available runtimes for a given specification major version.
     *
     * @param context      a Context to use when searching for runtimes.
     * @param majorVersion a major version number of OpenXR.
     * @return a list of runtimes, or null if something went wrong or none were found.
     */
    private fun getAvailableRuntimesFromMetadata(context: Context, majorVersion: Int, abi: String): List<RuntimeData>? {
        val runtimes = OpenXRLoaderUtils.findOpenXRRuntimes(context, majorVersion, abi)
        return if (runtimes != null && runtimes.isNotEmpty()) {
            runtimes
        } else null
    }


    /**
     * Get a list of available runtimes for a given specification major version.
     *
     * @param context      a Context to use when searching for runtimes.
     * @param majorVersion a major version number of OpenXR.
     * @param abi          the ABI to return data for.
     * @return a list of runtimes, or null if something went wrong or none were found.
     */
    override fun getAvailableRuntimes(context: Context, majorVersion: Int, abi: String): List<RuntimeData> {
        var runtimes = mutableListOf<RuntimeData>()
        getAvailableRuntimesFromMetadata(context, majorVersion, abi)?.let {
            runtimes.addAll(it)
        }
        // We recursively ask the system runtime broker if it has a runtime, so the loader only has
        // to talk to one broker, and the installable broker "owns" the user preference.
        getRuntimeFromContentProvider(context, majorVersion, abi, BrokerContract.BrokerType.SystemRuntimeBroker)?.let { runtimes.add(it) }
        return runtimes
    }

    companion object {
        const val TAG = "RuntimeEnumeratorImpl"
    }
}