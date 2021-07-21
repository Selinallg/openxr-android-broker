// Copyright 2020-2021, Collabora, Ltd.
// SPDX-License-Identifier: BSL-1.0

package org.khronos.openxr.runtime_broker.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface RuntimeChooser {
    /**
     * Get the currently active runtime for a given specification major version.
     * <p>
     * The first one of these is the preferred/"active" runtime
     *
     * @param context      a Context to use when searching for runtimes.
     * @param majorVersion a major version number of OpenXR.
     * @param abi          the ABI to return data for.
     * @return the active runtime, or null if something went wrong or none were found.
     */
    @Nullable
    RuntimeData
    getActiveRuntime(@NonNull Context context, int majorVersion, @NonNull String abi);

}
