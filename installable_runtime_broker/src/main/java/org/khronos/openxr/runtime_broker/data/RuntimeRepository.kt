// Copyright 2021, Qualcomm Innovation Center, Inc.
// SPDX-License-Identifier: BSL-1.0
package org.khronos.openxr.runtime_broker.data

/**
 * Interface definition for query/update runtime information.
 */
interface RuntimeRepository {
    /**
     * Get installed runtimes.
     *
     * @return List of runtime information.
     */
    fun getInstalledRuntimes(): List<RuntimeInformation>

    /**
     * Query if given package name is the one selected by user.
     *
     * @param packageName Package name used for query.
     * @return true if given package is selected; otherwise false.
     */
    fun isSelected(packageName: String): Boolean

    /**
     * Get user-selected runtime.
     *
     * @return Package name of user-selected runtime.
     */
    fun getSelectedRuntime(): String

    /**
     * Update selected runtime to given package name.
     *
     * @param packageName Package name of selected runtime.
     */
    fun updateRuntimeSelection(packageName: String, selected: Boolean)

}