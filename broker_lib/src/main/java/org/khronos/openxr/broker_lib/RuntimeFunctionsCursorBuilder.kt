// Copyright 2020, Collabora, Ltd.
// SPDX-License-Identifier: BSL-1.0
package org.khronos.openxr.broker_lib

import android.database.MatrixCursor
import org.khronos.openxr.broker_lib.RuntimeFunctionsCursorBuilder.ColumnFiller
import org.khronos.openxr.runtime_broker.utils.BrokerContract
import org.khronos.openxr.runtime_broker.utils.RuntimeData
import java.security.InvalidParameterException
import java.util.*
import java.util.stream.Collectors

internal class RuntimeFunctionsCursorBuilder(runtime: RuntimeData?, projection: Array<String>) {
    companion object {
        private val columnFillerHashMap = HashMap<String, ColumnFiller?>()

        init {
            columnFillerHashMap[BrokerContract.Functions.Columns._ID] = ColumnFiller { item: Int, _: Entry -> item }
            columnFillerHashMap[BrokerContract.Functions.Columns.FUNCTION_NAME] = ColumnFiller { _: Int, data: Entry -> data.functionName }
            columnFillerHashMap[BrokerContract.Functions.Columns.SYMBOL_NAME] = ColumnFiller { _: Int, data: Entry -> data.symbolName }
        }
    }

    private val columnFillers = ArrayList<ColumnFiller>()
    val cursor: MatrixCursor
    private var functions: List<Entry> = ArrayList()
    fun addRow(itemNum: Int) {
        if (itemNum >= functions.size) {
            return
        }
        val entry = functions[itemNum]
        val row = ArrayList<Any?>()
        for (filler in columnFillers) {
            row.add(filler.getColumnData(itemNum, entry))
        }
        cursor.addRow(row)
    }

    fun addAllRows() {
        if (functions.isEmpty()) {
            return
        }
        for (i in functions.indices) addRow(i)
    }

    private fun interface ColumnFiller {
        fun getColumnData(itemNum: Int, data: Entry): Any?
    }

    private class Entry(val functionName: String, val symbolName: String)

    init {
        if (runtime != null) {
            functions = runtime.functions
                    .entries
                    .stream()
                    .sorted()
                    .filter { entry: Map.Entry<String?, String?> -> entry.key != null && entry.value != null }
                    .map { entry: Map.Entry<String, String> -> Entry(entry.key, entry.value) }
                    .collect(Collectors.toList())
        }
        for (column in projection) {
            val filler = columnFillerHashMap.getOrDefault(column, null)
                    ?: throw InvalidParameterException("Invalid column name passed: $column")
            columnFillers.add(filler)
        }
        cursor = MatrixCursor(projection)
    }
}