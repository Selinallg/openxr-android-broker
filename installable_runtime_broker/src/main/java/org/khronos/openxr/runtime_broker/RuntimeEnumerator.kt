// Copyright 2020-2021, Collabora, Ltd.
// SPDX-License-Identifier: BSL-1.0

package org.khronos.openxr.runtime_broker

import android.content.Context
import org.khronos.openxr.runtime_broker.utils.RuntimeData

interface RuntimeEnumerator {

    /**
     * Get a list of available runtimes for a given specification major version.
     *
     * @param context      a Context to use when searching for runtimes.
     * @param majorVersion a major version number of OpenXR.
     * @param abi          the ABI to return data for.
     * @return a list of runtimes, or null if something went wrong or none were found.
     */
    fun getAvailableRuntimes(context: Context, majorVersion: Int, abi: String): List<RuntimeData>
}