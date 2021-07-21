// Copyright 2020, Collabora, Ltd.
// SPDX-License-Identifier: BSL-1.0

@file:Suppress("MemberVisibilityCanBePrivate")

package org.khronos.openxr.broker_lib

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.Keep
import androidx.annotation.NonNull
import org.khronos.openxr.runtime_broker.utils.RuntimeData
import java.util.*

/**
 * Helper utilities for finding OpenXR runtimes.
 *
 *
 * You probably want to add the following to your manifest if you use this, for Android 11+
 * compatibility:
 * <pre>`<queries>
 * <intent>
 * <action android:name="org.khronos.openxr.OpenXRRuntimeService" />
 * </intent>
 * </queries>
`</pre> *
 */
object OpenXRLoaderUtils {
    private const val TAG = "OpenXRLoaderUtils"
    const val serviceName = "org.khronos.openxr.OpenXRRuntimeService"

    /**
     * Find all installed OpenXR runtimes, even inactive ones.
     *
     * @param context the Context to use to perform the lookup.
     * @return A list of the RuntimeData corresponding to the runtimes, or null if there are no
     * OpenXR runtimes.
     */
    fun findOpenXRRuntimes(context: Context, @NonNull abi: String): MutableList<RuntimeData>? {
        val packageManager = context.packageManager
        val intent = Intent(serviceName)
        val resolutions = packageManager.queryIntentServices(
            intent,
            PackageManager.GET_META_DATA + PackageManager.GET_SHARED_LIBRARY_FILES
        )
        if (resolutions.size == 0) {
            Log.w(TAG, "got no services for our intent.")
            return null
        }
        val runtimes = ArrayList<RuntimeData>()
        for (resolveInfo in resolutions) {
            Log.i(
                TAG, "Considering intent service resolution: " +
                        resolveInfo.serviceInfo.applicationInfo.packageName
            )
            val runtimeData = RuntimeData.fromIntentResolveInfo(resolveInfo, abi)
            runtimeData ?: continue
            Log.i(
                TAG, String.format(
                    "Runtime SO for %s is '%s', OpenXR major version %d",
                    resolveInfo.serviceInfo.applicationInfo.packageName,
                    runtimeData.soFilename, runtimeData.majorVersion
                )
            )
            runtimes.add(runtimeData)
        }
        if (runtimes.isEmpty()) {
            Log.w(TAG, "No OpenXR runtimes found.")
            return null
        }
        return runtimes
    }

    /**
     * Find all installed OpenXR runtimes, even inactive ones, of a given major version.
     *
     * @param context      the Context to use to perform the lookup.
     * @param majorVersion the OpenXR major version to find
     * @return A list of the ResolveInfo corresponding to the runtimes, or null if there are no
     * OpenXR runtimes of the given version.
     */
    @Keep
    fun findOpenXRRuntimes(
        context: Context,
        majorVersion: Int,
        @NonNull abi: String
    ): List<RuntimeData>? {
        val runtimes = findOpenXRRuntimes(context, abi) ?: return null
        runtimes.removeIf { data: RuntimeData -> data.majorVersion != majorVersion.toLong() }
        if (runtimes.isEmpty()) {
            Log.w(TAG, String.format("No OpenXR runtimes of major version %d found.", majorVersion))
            return null
        }
        return runtimes
    }

}