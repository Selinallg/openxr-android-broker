// Copyright 2020-2021, Collabora, Ltd.
// SPDX-License-Identifier: BSL-1.0

#include <jnipp.h>
#include <jni.h>

#include <wrap/android.net.h>
#include <wrap/android.content.h>
#include <wrap/android.database.h>

#include <android/log.h>

#include <sstream>

using wrap::android::content::ContentUris;
using wrap::android::content::Context;
using wrap::android::database::Cursor;
using wrap::android::net::Uri;
using wrap::android::net::Uri_Builder;

// Code in here corresponds roughly to the Java "BrokerContract" class and subclasses.
namespace
{
    static constexpr auto AUTHORITY = "org.khronos.openxr.runtime_broker";
    static constexpr auto BASE_PATH = "openxr";
    static constexpr auto ABI_PATH = "abi";
    static constexpr auto RUNTIMES_PATH = "runtimes";

    struct BaseColumns
    {
        /**
         * The unique ID for a row.
         */
        static constexpr auto ID = "_id";
    };

    /**
     * Contains details for the /openxr/[major_ver]/abi/[abi]/runtimes/active URI.
     * <p>
     * This URI represents a "table" containing at most one item, the currently active runtime. The
     * policy of which runtime is chosen to be active (if more than one is installed) is left to the
     * content provider.
     * <p>
     * No sort order is required to be honored by the content provider.
     */
    namespace active_runtime
    {
        /**
         * Final path component to this URI.
         */
        static constexpr auto TABLE_PATH = "active";

        /**
         * Create a content URI for querying the data on the active runtime for a
         * given major version of OpenXR.
         *
         * @param majorVer The major version of OpenXR.
         * @return A content URI for a single item: the active runtime.
         */
        static Uri makeContentUri(int majorVersion, const char *abi)
        {
            auto builder = Uri_Builder::construct();
            builder.scheme("content")
                .authority(AUTHORITY)
                .appendPath(BASE_PATH)
                .appendPath(std::to_string(majorVersion))
                .appendPath(ABI_PATH)
                .appendPath(abi)
                .appendPath(RUNTIMES_PATH)
                .appendPath(TABLE_PATH);
            ContentUris::appendId(builder, 0);
            return builder.build();
        }

        struct Columns : BaseColumns
        {
            /**
             * Constant for the PACKAGE_NAME column name
             */
            static constexpr auto PACKAGE_NAME = "package_name";

            /**
             * Constant for the NATIVE_LIB_DIR column name
             */
            static constexpr auto NATIVE_LIB_DIR = "native_lib_dir";

            /**
             * Constant for the SO_FILENAME column name
             */
            static constexpr auto SO_FILENAME = "so_filename";

            /**
             * Constant for the HAS_FUNCTIONS column name.
             * <p>
             * If this column contains true, you should check the /functions/ URI for that runtime.
             */
            static constexpr auto HAS_FUNCTIONS = "has_functions";
        };
    } // namespace active_runtime

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
    namespace functions
    {
        /**
         * Final path component to this URI.
         */
        static constexpr auto
            TABLE_PATH = "functions";

        /**
         * Create a content URI for querying all rows of the function remapping data for a given
         * runtime package and major version of OpenXR.
         *
         * @param majorVer    The major version of OpenXR.
         * @param packageName The package name of the runtime.
         * @return A content URI for the entire table: the function remapping for that runtime.
         */
        static Uri makeContentUri(int majorVersion, std::string const &packageName, const char *abi)
        {
            auto builder = Uri_Builder::construct();
            builder.scheme("content")
                .authority(AUTHORITY)
                .appendPath(BASE_PATH)
                .appendPath(std::to_string(majorVersion))
                .appendPath(ABI_PATH)
                .appendPath(abi)
                .appendPath(RUNTIMES_PATH)
                .appendPath(packageName)
                .appendPath(TABLE_PATH);
            return builder.build();
        }

        struct Columns : BaseColumns
        {

            /**
             * Constant for the FUNCTION_NAME column name
             */
            static constexpr auto
                FUNCTION_NAME = "function_name";

            /**
             * Constant for the SYMBOL_NAME column name
             */
            static constexpr auto
                SYMBOL_NAME = "symbol_name";
        };
    } // namespace functions

} // namespace

static inline jni::Array<std::string> makeArray(std::initializer_list<const char *> &&list)
{
    auto ret = jni::Array<std::string>{(long)list.size()};
    long i = 0;
    for (auto &&elt : list)
    {
        ret.setElement(i, elt);
        ++i;
    }
    return ret;
}

static constexpr auto TAG = "OpenXR-Loader";

#if defined(__arm__)
static constexpr auto ABI = "armeabi-v7l";
#elif defined(__aarch64__)
static constexpr auto ABI = "arm64-v8a";
#elif defined(__i386__)
static constexpr auto ABI = "x86";
#elif defined(__x86_64__)
static constexpr auto ABI = "x86_64";
#else
#error "Unknown ABI!"
#endif

extern "C" jstring
Java_org_khronos_openxr_nativesampleclient_MainActivity_getRuntime(JNIEnv *env, jclass clazz,
                                                                   jobject context)
{
    jni::init(env);
    try
    {
        jni::Array<std::string> projection = makeArray(
            {active_runtime::Columns::ID,
             active_runtime::Columns::PACKAGE_NAME,
             active_runtime::Columns::NATIVE_LIB_DIR,
             active_runtime::Columns::SO_FILENAME,
             active_runtime::Columns::HAS_FUNCTIONS});

        auto uri = active_runtime::makeContentUri(1, ABI);
        Cursor cursor = Context{context}.getContentResolver().query(
            uri, projection);
        if (cursor.isNull())
        {
            __android_log_write(ANDROID_LOG_INFO, TAG,
                                "Null cursor when querying content resolver.");
            return nullptr;
        }
        if (cursor.getCount() < 1)
        {
            __android_log_write(ANDROID_LOG_INFO, TAG,
                                "Non-null but empty cursor when querying content resolver.");
            return nullptr;
        }
        cursor.moveToFirst();

        auto filename = cursor.getString(
            cursor.getColumnIndex(active_runtime::Columns::SO_FILENAME));
        auto packageName = cursor.getString(
            cursor.getColumnIndex(active_runtime::Columns::PACKAGE_NAME));
        auto hasFunctions =
            cursor.getInt(cursor.getColumnIndex(active_runtime::Columns::HAS_FUNCTIONS)) == 1;
        std::ostringstream os;
        os << "Found runtime so " << filename << " in package " << packageName
           << (hasFunctions ? " with function/symbol mapping defined"
                            : " with no function/symbol mapping changes");
        return env->NewStringUTF(os.str().c_str());
    }
    catch (std::exception const &e)
    {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Exception when searching for runtime: %s",
                            e.what());
        return nullptr;
    }
}