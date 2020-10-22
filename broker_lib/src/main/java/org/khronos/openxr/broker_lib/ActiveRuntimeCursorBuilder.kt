// Copyright 2020, Collabora, Ltd.
// SPDX-License-Identifier: BSL-1.0
package org.khronos.openxr.broker_lib

import android.database.MatrixCursor
import org.khronos.openxr.broker_lib.ActiveRuntimeCursorBuilder.ColumnFiller
import org.khronos.openxr.runtime_broker.utils.BrokerContract
import org.khronos.openxr.runtime_broker.utils.RuntimeData
import java.security.InvalidParameterException
import java.util.*

internal class ActiveRuntimeCursorBuilder(private val runtimes: List<RuntimeData>?, projection: Array<String>) {
    companion object {
        private val columnFillerHashMap = HashMap<String, ColumnFiller?>()

        init {
            columnFillerHashMap[BrokerContract.ActiveRuntime.Columns._ID] = ColumnFiller { item: Int, _: RuntimeData -> item }
            columnFillerHashMap[BrokerContract.ActiveRuntime.Columns.PACKAGE_NAME] = ColumnFiller { _: Int, data: RuntimeData -> data.packageName }
            columnFillerHashMap[BrokerContract.ActiveRuntime.Columns.NATIVE_LIB_DIR] = ColumnFiller { _: Int, data: RuntimeData -> data.nativeLibraryDir }
            columnFillerHashMap[BrokerContract.ActiveRuntime.Columns.SO_FILENAME] = ColumnFiller { _: Int, data: RuntimeData -> data.soFilename }
            columnFillerHashMap[BrokerContract.ActiveRuntime.Columns.HAS_FUNCTIONS] = ColumnFiller { _: Int, data: RuntimeData -> if (data.functions.isEmpty()) 0 else 1 }
        }
    }

    private val columnFillers = ArrayList<ColumnFiller>()
    val cursor: MatrixCursor
    fun addRow(itemNum: Int) {
        runtimes ?: return

        if (itemNum >= runtimes.size) {
            return
        }
        val data = runtimes[itemNum]
        val row = ArrayList<Any?>(columnFillers.size)
        for (filler in columnFillers) {
            row.add(filler.getColumnData(itemNum, data))
        }
        cursor.addRow(row)
    }

    fun addRow(itemNum: Int, data: RuntimeData) {
        if (runtimes != null) {
            return
        }
        val row = Vector<Any?>()
        for (filler in columnFillers) {
            row.add(filler.getColumnData(itemNum, data))
        }
        cursor.addRow(row)
    }

    private fun interface ColumnFiller {
        fun getColumnData(itemNum: Int, data: RuntimeData): Any?
    }

    init {
        for (column in projection) {
            val filler = columnFillerHashMap.getOrDefault(column, null)
                    ?: throw InvalidParameterException("Invalid column name passed: $column")
            columnFillers.add(filler)
        }
        cursor = MatrixCursor(projection)
    }
}