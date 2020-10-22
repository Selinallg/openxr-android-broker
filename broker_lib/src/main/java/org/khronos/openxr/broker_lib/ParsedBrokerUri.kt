// Copyright 2020, Collabora, Ltd.
// SPDX-License-Identifier: BSL-1.0
package org.khronos.openxr.broker_lib

import android.os.Build
import org.khronos.openxr.runtime_broker.utils.BrokerContract

enum class TableType {
    ActiveRuntime, Functions
}

/**
 * All data from a runtime broker content-provider URI, parsed out.
 */
class ParsedBrokerUri {

    /**
     * Which authority was queried.
     */
    @JvmField
    var brokerType: BrokerContract.BrokerType = BrokerContract.BrokerType.RuntimeBroker

    /**
     * Which type of table is requested.
     */
    @JvmField
    var tableType = TableType.ActiveRuntime

    /**
     * OpenXR major version
     */
    @JvmField
    var majorVer = 0

    /**
     * The package name, only valid for tableType == TableType.Functions.
     */
    @JvmField
    var packageName: String? = null

    /**
     * The ABI to get the runtime path for.
     */
    @JvmField
    var abi: String = Build.SUPPORTED_ABIS[0]

    /**
     * Row ID - if null, that implies the URI was for a dir.
     */
    @JvmField
    var row: Int? = null

    /**
     * True if the URI is for the directory, and not just a single row.
     */
    val isDir: Boolean
        get() = row == null


}