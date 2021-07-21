// Copyright 2020-2021, Collabora, Ltd.
// SPDX-License-Identifier: BSL-1.0

package org.khronos.openxr.runtime_broker.utils;

import android.content.ContentUris;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class BrokerContract {
    /**
     * The URI authority for the user-preference-controlled installable broker.
     */
    public static final String AUTHORITY = "org.khronos.openxr.runtime_broker";
    /**
     * The URI authority for the system/vendor-provided broker.
     */
    public static final String SYSTEM_AUTHORITY = "org.khronos.openxr.system_runtime_broker";
    // path components used by multiple table types.
    public static final String BASE_PATH = "openxr";
    public static final String ABI_PATH = "abi";
    public static final String RUNTIMES_PATH = "runtimes";
    public static final String CONTENT_SCHEME = "content";

    // do not instantiate
    private BrokerContract() {
    }

    public static String brokerToAuthority(BrokerType brokerType) {
        switch (brokerType) {
            case RuntimeBroker:
                return AUTHORITY;
            case SystemRuntimeBroker:
                return SYSTEM_AUTHORITY;
        }
        throw new IllegalArgumentException();
    }

    public enum BrokerType {
        RuntimeBroker,
        SystemRuntimeBroker,
    }

    /**
     * Contains details for the /openxr/[major_ver]/abi/[abi]/runtimes/active URI.
     * <p>
     * This URI represents a "table" containing at most one item, the currently active runtime. The
     * policy of which runtime is chosen to be active (if more than one is installed) is left to the
     * content provider.
     * <p>
     * No sort order is required to be honored by the content provider.
     */
    public static final class ActiveRuntime {

        /**
         * Final path component to this URI.
         */
        public static final String TABLE_PATH = "active";

        /**
         * Create a content URI for querying the data on the active runtime for a
         * given major version of OpenXR.
         *
         * @param brokerType The broker type (regular/installable or system) to retrieve paths for
         * @param majorVer   The major version of OpenXR.
         * @param abi        The Android ABI to retrieve paths for
         * @return A content URI for a single item: the active runtime.
         */
        @NonNull
        public static Uri makeContentUri(BrokerType brokerType, int majorVer, @Nullable String abi) {
            if (abi == null) {
                abi = Build.SUPPORTED_ABIS[0];
            }
            Uri.Builder builder = new Uri.Builder();
            builder.scheme(CONTENT_SCHEME)
                    .authority(brokerToAuthority(brokerType))
                    .appendPath(BASE_PATH)
                    .appendPath(String.valueOf(majorVer))
                    .appendPath(ABI_PATH)
                    .appendPath(abi)
                    .appendPath(RUNTIMES_PATH)
                    .appendPath(TABLE_PATH);
            ContentUris.appendId(builder, 0);
            return builder.build();
        }

        /**
         * Contains the constants used for provider response columns in the /active URI.
         */
        public static final class Columns implements BaseColumns {
            /**
             * Constant for the PACKAGE_NAME column name containing the Android package name
             */
            public static final String PACKAGE_NAME = "package_name";
            /**
             * Constant for the NATIVE_LIB_DIR column name, containing the ABI-specific absolute
             * path to the directory containing the dynamic library to load.
             */
            public static final String NATIVE_LIB_DIR = "native_lib_dir";
            /**
             * Constant for the SO_FILENAME column name, containing the filename of the dynamic library to load.
             */
            public static final String SO_FILENAME = "so_filename";
            /**
             * Constant for the HAS_FUNCTIONS column name.
             * <p>
             * If this column contains true, you should check the /functions/ URI for that runtime.
             */
            public static final String HAS_FUNCTIONS = "has_functions";

            // do not instantiate
            private Columns() {
            }
        }
    }


    /**
     * Contains details for the /openxr/[major_ver]/abi/[abi]/runtimes/[package]/functions URI.
     * <p>
     * This URI is for package-specific function name remapping. Since this is an optional field in
     * the corresponding JSON manifests for OpenXR, it is optional here as well. If the active
     * runtime contains "true" in its "has_functions" column, then this table must exist and be
     * queryable.
     * <p>
     * No sort order is required to be honored by the content provider.
     */
    public static final class Functions {

        /**
         * Final path component to this URI.
         */
        public static final String TABLE_PATH = "functions";

        /**
         * Create a content URI for querying all rows of the function remapping data for a given
         * runtime package and major version of OpenXR.
         *
         * @param brokerType  The broker type (regular/installable or system) to retrieve paths for
         * @param majorVer    The major version of OpenXR.
         * @param packageName The package name of the runtime.
         * @param abi         The ABI to query.
         * @return A content URI for the entire table: the function remapping for that runtime.
         */
        @NonNull
        public static Uri makeContentUri(BrokerType brokerType, int majorVer, String packageName, @Nullable String abi) {
            if (abi == null) {
                abi = Build.SUPPORTED_ABIS[0];
            }
            Uri.Builder builder = new Uri.Builder();
            builder.scheme(CONTENT_SCHEME)
                    .authority(brokerToAuthority(brokerType))
                    .appendPath(BASE_PATH)
                    .appendPath(String.valueOf(majorVer))
                    .appendPath(ABI_PATH)
                    .appendPath(abi)
                    .appendPath(RUNTIMES_PATH)
                    .appendPath(packageName)
                    .appendPath(TABLE_PATH);
            return builder.build();
        }

        /**
         * Contains the constants used for provider response columns in the .../functions URI.
         */
        public static final class Columns implements BaseColumns {
            /**
             * Constant for the FUNCTION_NAME column name, containing the function name as found in
             * the specification documents.
             */
            public static final String FUNCTION_NAME = "function_name";
            /**
             * Constant for the SYMBOL_NAME column name, containing the symbol to load from the
             * dynamic library.
             */
            public static final String SYMBOL_NAME = "symbol_name";

            // do not instantiate
            private Columns() {
            }
        }
    }


}
