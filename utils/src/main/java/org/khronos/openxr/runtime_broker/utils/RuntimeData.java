// Copyright 2020, Collabora, Ltd.
// SPDX-License-Identifier: BSL-1.0

package org.khronos.openxr.runtime_broker.utils;


import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Data corresponding to a single OpenXR runtime.
 * <p>
 * Populated from service-associated metadata keys, this data is the rough equivalent of the data
 * supported by the JSON manifest format used by the OpenXR loader on other platforms.
 */
public final class RuntimeData {
    private static final String TAG = "RuntimeData";

    private static final String soMetadataName = "org.khronos.openxr.OpenXRRuntime.SoFilename";
    private static final String versionMetadataName = "org.khronos.openxr.OpenXRRuntime.MajorVersion";
    private static final String functionsMetadataPrefix = "org.khronos.openxr.OpenXRRuntime.Functions.";
    private static final Pattern functionNameRegex = Pattern.compile("xr[A-Z]([a-z0-9]*)([0-9A-Z]([a-z0-9]*))*");
    /**
     * The ServiceInfo used to locate the runtime, if available.
     */
    public final @Nullable
    ServiceInfo serviceInfo;

    /**
     * The package name associated with the runtime
     */
    public final @NonNull
    String packageName;

    /**
     * The native library directory.
     *
     * @todo This might carry the wrong ABI-specific suffix, how to fix?
     */
    public final @NonNull
    String nativeLibraryDir;

    /**
     * The filename of the .so file containing the runtime entry points.
     * <p>
     * Indicated by the "org.khronos.openxr.OpenXRRuntime.SoFilename" meta-data string value on the
     * "org.khronos.openxr.OpenXRRuntimeService" service manifest.
     */
    public final @NonNull
    String soFilename;
    /**
     * The major version of OpenXR implemented.
     * <p>
     * Indicated by the "org.khronos.openxr.OpenXRRuntime.MajorVersion" meta-data string value on
     * the "org.khronos.openxr.OpenXRRuntimeService" service manifest.
     */
    public final long majorVersion;
    /**
     * A map of function names to symbol names, akin to the object of the same name in the JSON
     * manifest format.
     * <p>
     * May be empty, as the corresponding JSON object is optional and may be empty.
     * <p>
     * Indicated by metadata string values  on the "org.khronos.openxr.OpenXRRuntimeService" service
     * manifest with keys starting with "org.khronos.openxr.OpenXRRuntime.Functions." The remaining
     * part of the meta-data key is taken to be the key in this map (the specified function name),
     * while the value is the symbol name that should be used.
     */
    public final @NonNull
    Map<String, String> functions;

    /**
     * The broker type returning this runtime.
     * <p>
     * Usually can be ignored. Used in the implementation of the installable runtime broker to proxy
     * the results of the system runtime broker.
     */
    public final @NonNull
    BrokerContract.BrokerType brokerType;

    /**
     * Constructor
     *
     * @param packageName      the package name associated with the OpenXR runtime
     * @param nativeLibraryDir Full path to the directory containing the .so file
     * @param soFilename       the filename of the .so file containing the runtime entry points
     * @param majorVersion     the major version of OpenXR implemented
     * @param functions        Function name map
     * @param brokerType       What type of broker returns this. If null, the default (installable) is assumed.
     */
    public RuntimeData(@NonNull String packageName,
                       @NonNull String nativeLibraryDir,
                       @NonNull String soFilename,
                       int majorVersion,
                       @NonNull Map<String, String> functions,
                       @Nullable BrokerContract.BrokerType brokerType) {
        this.serviceInfo = null;
        this.packageName = packageName;
        this.nativeLibraryDir = nativeLibraryDir;
        this.soFilename = soFilename;
        this.majorVersion = majorVersion;
        this.functions = functions;
        if (brokerType == null) {
            this.brokerType = BrokerContract.BrokerType.RuntimeBroker;
        } else {
            this.brokerType = brokerType;
        }
    }

    /**
     * Constructor
     *
     * @param serviceInfo      the ServiceInfo associated with the OpenXR runtime
     * @param nativeLibraryDir Full path to the directory containing the .so file
     * @param soFilename       the filename of the .so file containing the runtime entry points
     * @param majorVersion     the major version of OpenXR implemented
     * @param functions        Function name map
     * @param brokerType       What type of broker returns this. If null, the default (installable) is assumed.
     */
    public RuntimeData(@NonNull ServiceInfo serviceInfo,
                       @NonNull String nativeLibraryDir,
                       @NonNull String soFilename,
                       int majorVersion,
                       @NonNull Map<String, String> functions,
                       @Nullable BrokerContract.BrokerType brokerType
    ) {
        this.serviceInfo = serviceInfo;
        this.packageName = serviceInfo.packageName;
        this.nativeLibraryDir = nativeLibraryDir;
        this.soFilename = soFilename;
        this.majorVersion = majorVersion;
        this.functions = functions;
        if (brokerType == null) {
            this.brokerType = BrokerContract.BrokerType.RuntimeBroker;
        } else {
            this.brokerType = brokerType;
        }
    }

    private static @Nullable
    String parseFunctionMetadataKey(@NonNull String key) {
        if (!key.startsWith(functionsMetadataPrefix)) {
            return null;
        }
        String funcName = key.substring(functionsMetadataPrefix.length());
        // for safety, check format of key
        if (functionNameRegex.matcher(key).matches()) {
            return funcName;
        }
        return null;
    }

    /**
     * Get the ComponentName for a Service
     *
     * @param resolveInfo a ResolveInfo from PackageManager.queryIntentServices() with an
     *                    org.khronos.openxr.OpenXRRuntimeService Intent and
     *                    PackageManager.GET_META_DATA
     * @return the ComponentName, or null if resolveInfo.serviceInfo is null
     */
    @Nullable
    private static ComponentName getServiceComponent(@NonNull ResolveInfo resolveInfo) {
        ServiceInfo serviceInfo = resolveInfo.serviceInfo;
        if (serviceInfo == null) {
            return null;
        }
        if (serviceInfo.packageName == null || serviceInfo.name == null) {
            return null;
        }
        return new ComponentName(serviceInfo.packageName, serviceInfo.name);
    }

    /**
     * Get the functions map, if any, for an OpenXR runtime.
     *
     * @param bundle the metadata bundle from the service resolve info.
     * @return a map of specified function name to exposed symbol, if any were specified in the
     * associated manifest meta-data. May be empty.
     */
    @NonNull
    private static Map<String, String> getFunctionsMap(@NonNull Bundle bundle) {

        HashMap<String, String> functions = new HashMap<>();
        for (String key : bundle.keySet()) {
            String function = parseFunctionMetadataKey(key);
            if (function == null) {
                continue;
            }
            String symbol;
            try {
                symbol = bundle.getString(key);
            } catch (ClassCastException e) {
                continue;
            }
            if (function != null && symbol != null && !symbol.isEmpty()) {
                functions.putIfAbsent(function, symbol);
            }
        }
        return functions;
    }

    /**
     * Get the runtime data for an OpenXR runtime.
     *
     * @param resolveInfo a ResolveInfo from PackageManager.queryIntentServices() with an
     *                    org.khronos.openxr.OpenXRRuntimeService Intent and
     *                    PackageManager.GET_META_DATA
     * @return the data object, or null if there was an issue, including if the service was not an
     * OpenXR runtime.
     */
    @Nullable
    public static RuntimeData fromIntentResolveInfo(@NonNull ResolveInfo resolveInfo, @NonNull String abi) {
        ServiceInfo serviceInfo = resolveInfo.serviceInfo;
        if (serviceInfo == null) {
            return null;
        }
        ApplicationInfo applicationInfo = serviceInfo.applicationInfo;
        if (applicationInfo == null) {
            return null;
        }
        Bundle bundle = serviceInfo.metaData;
        if (bundle == null) {
            Log.w(TAG, "No service metadata for " + serviceInfo.name);
            return null;
        }
        ServiceInfo info = resolveInfo.serviceInfo;
        if (info == null) {
            return null;
        }
        if (!bundle.containsKey(soMetadataName)) {
            return null;
        }
        if (!bundle.containsKey(versionMetadataName)) {
            return null;
        }
        Map<String, String> functions = getFunctionsMap(bundle);
        return new RuntimeData(
                info,
                getAbiNativeLibraryDir(applicationInfo, abi),
                bundle.getString(soMetadataName),
                bundle.getInt(versionMetadataName),
                functions,
                null);
    }

    private static String getAbiNativeLibraryDir(@NonNull ApplicationInfo applicationInfo, @NonNull String abi) {
        // STOPSHIP: 11/11/2020 this isn't doing anything right now, but should.
        // unfortunately the useful members of ApplicationInfo are marked UnsupportedAppUsage and Hide
        return applicationInfo.nativeLibraryDir;
    }
}

