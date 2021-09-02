// Copyright 2021, Qualcomm Innovation Center, Inc.
// SPDX-License-Identifier: BSL-1.0
package org.khronos.openxr.runtime_broker.data

import android.graphics.drawable.Drawable
import java.text.DateFormat
import java.util.Date

data class RuntimeInformation(
    val applicationName: String,
    val packageName: String,
    val icon: Drawable,
    val majorVersion: Long,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    var selected: Boolean
)

fun RuntimeInformation.lastUpdatedDate(): String = DateFormat.getDateInstance().format(Date(lastUpdateTime));
