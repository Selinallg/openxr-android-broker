// Copyright 2021, Qualcomm Innovation Center, Inc.
// SPDX-License-Identifier: BSL-1.0
package org.khronos.openxr.runtime_broker.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.khronos.openxr.broker_lib.OpenXRLoaderUtils
import org.khronos.openxr.runtime_broker.BuildConfig
import org.khronos.openxr.runtime_broker.utils.RuntimeData
import javax.inject.Inject

class RuntimeRepositoryImpl @Inject constructor(private val context: Context) : RuntimeRepository {
    override fun getInstalledRuntimes(): List<RuntimeInformation> {
        val runtimes = OpenXRLoaderUtils.findOpenXRRuntimes(context)
        return if (runtimes != null && runtimes.isNotEmpty()) {
            val runtimeInfoList = mutableListOf<RuntimeInformation>()
            for (runtime in runtimes) {
                runtimeInfoList.add(runtime.toRuntimeInformation(context))
            }

            // Sorted by version then last update time
            runtimeInfoList.sortWith(compareByDescending<RuntimeInformation> { it.majorVersion }.thenByDescending { it.lastUpdateTime })
            runtimeInfoList
        } else listOf()
    }

    private fun RuntimeData.toRuntimeInformation(context: Context): RuntimeInformation {
        val applicationName = context.packageManager.getApplicationInfo(packageName, 0)
            .let { info -> context.packageManager.getApplicationLabel(info) }
        val icon = context.packageManager.getApplicationIcon(packageName)
        val packageInfo = context.packageManager.getPackageInfo(packageName, 0)

        return RuntimeInformation(
            applicationName as String,
            packageName,
            icon,
            majorVersion,
            packageInfo.firstInstallTime,
            packageInfo.lastUpdateTime,
            isSelected(packageName)
        )
    }

    override fun isSelected(packageName: String): Boolean {
        return getPreference(context).getString(PREFERENCE_KEY_SELECTED, "") == packageName
    }

    override fun getSelectedRuntime(): String {
        return getPreference(context).getString(PREFERENCE_KEY_SELECTED, "")!!
    }

    override fun updateRuntimeSelection(packageName: String, selected: Boolean) {
        val editor = getPreference(context).edit()
        if (selected) {
            editor.putString(PREFERENCE_KEY_SELECTED, packageName)
        } else {
            editor.remove(PREFERENCE_KEY_SELECTED)
        }
        editor.apply()
    }

    private fun getPreference(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setUserAuthenticationRequired(false)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            "${BuildConfig.APPLICATION_ID}.preferences",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        private const val PREFERENCE_KEY_SELECTED = "selected_runtime"
    }
}
