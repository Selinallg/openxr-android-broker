// Copyright 2020, Collabora, Ltd.
// SPDX-License-Identifier: BSL-1.0

package org.khronos.openxr.runtime_broker.utils

import android.content.Context
import android.database.Cursor
import android.util.Log


private fun getRuntimeFunctions(type: BrokerContract.BrokerType, context: Context, majorVersion: Int, abi: String, pkg: String): Map<String, String> {
    val uri = BrokerContract.Functions.makeContentUri(type, majorVersion, abi, pkg)
    val projection = arrayOf(
            BrokerContract.Functions.Columns.FUNCTION_NAME,
            BrokerContract.Functions.Columns.SYMBOL_NAME)
    val cursor: Cursor = context.contentResolver.query(uri,
            projection,
            null,
            null,
            null) ?: return mapOf()

    val funcColumn = cursor.getColumnIndex(BrokerContract.Functions.Columns.FUNCTION_NAME)
    val symbolColumn = cursor.getColumnIndex(BrokerContract.Functions.Columns.SYMBOL_NAME)
    val map = mutableMapOf<String, String>()
    while (cursor.moveToNext()) {
        map[cursor.getString(funcColumn)] = cursor.getString(symbolColumn)
    }
    cursor.close()

    return map
}

fun getRuntimeFromContentProvider(type: BrokerContract.BrokerType, context: Context, majorVersion: Int, abi: String): RuntimeData? {
    val projection = arrayOf(
            BrokerContract.ActiveRuntime.Columns.PACKAGE_NAME,
            BrokerContract.ActiveRuntime.Columns.NATIVE_LIB_DIR,
            BrokerContract.ActiveRuntime.Columns.SO_FILENAME,
            BrokerContract.ActiveRuntime.Columns.HAS_FUNCTIONS)

    val uri = BrokerContract.ActiveRuntime.makeContentUri(type, majorVersion, abi)
    Log.d("getRuntimeFromContentProvider", "URI: $uri")
    val cursor: Cursor = context.contentResolver.query(uri,
            projection,
            null,
            null,
            null) ?: return null

    if (!cursor.moveToNext()) {
        return null
    }
    val packageName = cursor.getString(cursor.getColumnIndex(BrokerContract.ActiveRuntime.Columns.PACKAGE_NAME))
    val nativeLibDir = cursor.getString(cursor.getColumnIndex(BrokerContract.ActiveRuntime.Columns.NATIVE_LIB_DIR))
    val soFilename = cursor.getString(cursor.getColumnIndex(BrokerContract.ActiveRuntime.Columns.SO_FILENAME))
    val hasFunctions = cursor.getInt(cursor.getColumnIndex(BrokerContract.ActiveRuntime.Columns.HAS_FUNCTIONS))
    val functions = if (hasFunctions != 0) {
        getRuntimeFunctions(type, context, majorVersion, abi, packageName)
    } else {
        mapOf()
    }
    val data = RuntimeData(packageName, nativeLibDir, soFilename, majorVersion, functions, type)
    cursor.close()
    return data
}