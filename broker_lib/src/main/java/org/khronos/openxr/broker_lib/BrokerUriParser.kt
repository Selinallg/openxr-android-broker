// Copyright 2020, Collabora, Ltd.
// SPDX-License-Identifier: BSL-1.0
package org.khronos.openxr.broker_lib

import android.content.ContentUris
import android.content.UriMatcher
import android.net.Uri
import android.util.Log
import org.khronos.openxr.runtime_broker.utils.BrokerContract

/**
 * Helper utility for parsing runtime broker content-provider URIs.
 *
 * This stores some state to speed matching, so it is a class instead of just a free function.
 */
sealed class BrokerUriParser(private val brokerType: BrokerContract.BrokerType) {

    /**
     * Make a ParsedBrokerUri from a Uri.
     *
     * @param uri the content URI to parse.
     * @return the parsed data, or null if there was an error in parsing.
     */
    fun parse(uri: Uri): ParsedBrokerUri? {
        if (BrokerContract.CONTENT_SCHEME != uri.scheme) {
            Log.w(
                TAG,
                "URI scheme was not the expected '${BrokerContract.CONTENT_SCHEME}': ${uri.scheme}"
            )
            return null
        }
        if (uri.authority != authority) {
            Log.w(
                TAG,
                "URI authority was not the expected value ${authority}, but instead: ${uri.authority}"
            )
            return null
        }
        val segments = uri.pathSegments
        var isDir = false
        val data = ParsedBrokerUri()
        data.brokerType = brokerType
        when (uriMatcher.match(uri)) {
            ActiveRuntimeItemCode -> data.tableType = TableType.ActiveRuntime
            ActiveRuntimeDirCode -> {
                data.tableType = TableType.ActiveRuntime
                isDir = true
            }
            FunctionsItemCode -> data.tableType = TableType.Functions
            FunctionsDirCode -> {
                data.tableType = TableType.Functions
                isDir = true
            }
            else -> {
                Log.e(TAG, "UriMatch failed")
                return null
            }
        }
        // all start /openxr/[majorver]
        try {
            data.majorVer = segments[1].toInt()
        } catch (e: NumberFormatException) {
            Log.w(TAG, "Could not parse this as a major version number: ${segments[1]}")
            return null
        }

        // all continue .../abi/[abi]
        data.abi = segments[3]

        if (!isDir) {
            data.row = ContentUris.parseId(uri).toInt()
        }
        if (data.tableType == TableType.Functions) {
            // These continue .../runtimes/[packageName]
            data.packageName = segments[5]
        }
        return data
    }


    companion object {
        private const val ActiveRuntimeItemCode = 1
        private const val ActiveRuntimeDirCode = 2
        private const val FunctionsItemCode = 3
        private const val FunctionsDirCode = 4
        private const val TAG = "BrokerUriParser"
    }

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
    private val authority = BrokerContract.brokerToAuthority(brokerType)

    init {
        val runtimeBasePath =
            "/${BrokerContract.BASE_PATH}/#/${BrokerContract.ABI_PATH}/*/${BrokerContract.RUNTIMES_PATH}"
        val activeRuntimeDirPath = "${runtimeBasePath}/${BrokerContract.ActiveRuntime.TABLE_PATH}"
        Log.i(TAG, "activeRuntimeDirPath: $activeRuntimeDirPath")
        uriMatcher.addURI(
            authority,
            activeRuntimeDirPath,
            ActiveRuntimeDirCode
        )
        uriMatcher.addURI(
            authority,
            "${activeRuntimeDirPath}/#",
            ActiveRuntimeItemCode
        )

        val runtimeFunctionsDirPath = "${runtimeBasePath}/*/${BrokerContract.Functions.TABLE_PATH}"
        Log.i(TAG, "runtimeFunctionsDirPath: $runtimeFunctionsDirPath")
        uriMatcher.addURI(
            authority,
            runtimeFunctionsDirPath,
            FunctionsDirCode
        )
        uriMatcher.addURI(
            authority,
            "${runtimeFunctionsDirPath}/#",
            FunctionsItemCode
        )
    }
}

class RuntimeBrokerUriParser : BrokerUriParser(BrokerContract.BrokerType.RuntimeBroker)
class SystemRuntimeBrokerUriParser : BrokerUriParser(BrokerContract.BrokerType.SystemRuntimeBroker)
