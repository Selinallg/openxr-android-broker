// Copyright 2020, Collabora, Ltd.
// SPDX-License-Identifier: BSL-1.0
package org.khronos.openxr.broker_lib

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log
import org.khronos.openxr.runtime_broker.utils.BrokerContract
import org.khronos.openxr.runtime_broker.utils.RuntimeChooser

/**
 * Abstract implementation of an OpenXR "Runtime Broker" content provider.
 *
 * An implementation only needs to provide an implementation of the RuntimeChooser interface and a BrokerUriParser.
 */
abstract class AbstractRuntimeBroker : ContentProvider() {
    protected abstract val runtimeChooser: RuntimeChooser
    protected abstract val parser: BrokerUriParser

    /**
     * ContentProvider interface.
     */
    override fun getType(uri: Uri): String? {
        val parsed: ParsedBrokerUri = parser.parse(uri)
                ?: throw IllegalArgumentException("Could not parse URI $uri")
        return when (parsed.tableType) {
            TableType.ActiveRuntime -> makeMime(parsed.isDir, "activeRuntime")
            TableType.Functions -> makeMime(parsed.isDir, BrokerContract.Functions.TABLE_PATH)
        }
    }


    /**
     * ContentProvider interface where the actual work is done.
     */
    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        require((selection == null && selectionArgs == null)) { "selection not supported" }
        require(sortOrder == null) { "sortOrder not supported" }
        val parsed: ParsedBrokerUri = parser.parse(uri)
                ?: throw IllegalArgumentException("Could not parse URI $uri")
        return when (parsed.tableType) {
            TableType.ActiveRuntime -> queryActiveRuntime(parsed, projection)
            TableType.Functions -> queryFunctions(parsed, projection)
        }
    }

    /**
     * Provides the internals of query() for the activeRuntime URI.
     */
    private fun queryActiveRuntime(parsed: ParsedBrokerUri, projection: Array<String>?): Cursor? {
        val runtime = runtimeChooser.getActiveRuntime(
                context!!.applicationContext,
                parsed.majorVer, parsed.abi)
        val runtimeCursorBuilder = ActiveRuntimeCursorBuilder(null, projection!!)
        // This table only has one row, so asking for row 0 or asking for a dir (all rows)
        // are equivalent.
        val row = parsed.row ?: 0
        if (runtime != null && row == 0) {
            Log.i(TAG, "Returning runtime: ${runtime.nativeLibraryDir}  ${runtime.soFilename}")
            runtimeCursorBuilder.addRow(0, runtime)
        }
        return runtimeCursorBuilder.cursor
    }

    /**
     * Provides the internals of query() for the functions URIs.
     */
    private fun queryFunctions(parsed: ParsedBrokerUri, projection: Array<String>?): Cursor? {
        val appContext = context?.applicationContext ?: return null
        val runtime = runtimeChooser.getActiveRuntime(
                appContext,
                parsed.majorVer,
                parsed.abi) ?: return null
        if (runtime.packageName != parsed.packageName) {
            return null
        }
        val cursorBuilder = RuntimeFunctionsCursorBuilder(runtime, projection!!)
        val rowNum = parsed.row
        if (rowNum != null) {
            cursorBuilder.addRow(rowNum)
        } else {
            cursorBuilder.addAllRows()
        }
        return cursorBuilder.cursor
    }

    /**
     * ContentProvider interface: implemented as a no-op.
     */
    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {
        return 0
    }

    /**
     * ContentProvider interface: implemented as a no-op.
     */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    /**
     * ContentProvider interface: implemented as a no-op.
     */
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    /**
     * ContentProvider interface: implemented as a no-op.
     */
    override fun onCreate(): Boolean {
        return true
    }

    companion object {
        private const val TAG: String = "AbstractRuntimeBroker"

        /**
         * Helper used by getType.
         */
        private fun makeMime(isDir: Boolean, table: String): String {
            return String.format("vnd.android.cursor.%s/vnd.%s.%s",
                    if (isDir) "dir" else "item",
                    BrokerContract.AUTHORITY,
                    table
            )
        }
    }
}