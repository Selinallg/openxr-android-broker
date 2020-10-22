// Copyright 2020, Collabora, Ltd.
// SPDX-License-Identifier: BSL-1.0
// Author: Ryan Pavlik <ryan.pavlik@collabora.com>

#pragma once

#include "ObjectWrapperBase.h"

namespace wrap {
namespace android::database {
/*!
 * Wrapper for android.database.Cursor objects.
 */
class Cursor : public ObjectWrapperBase {
  public:
    using ObjectWrapperBase::ObjectWrapperBase;
    static constexpr const char *getTypeName() noexcept {
        return "android/database/Cursor";
    }

    /*!
     * Wrapper for the getCount method
     *
     * Java prototype:
     * `public abstract int getCount();`
     *
     * JNI signature: ()I
     *
     */
    int32_t getCount();

    /*!
     * Wrapper for the moveToFirst method
     *
     * Java prototype:
     * `public abstract boolean moveToFirst();`
     *
     * JNI signature: ()Z
     *
     */
    bool moveToFirst();

    /*!
     * Wrapper for the getColumnIndex method
     *
     * Java prototype:
     * `public abstract int getColumnIndex(java.lang.String);`
     *
     * JNI signature: (Ljava/lang/String;)I
     *
     */
    int32_t getColumnIndex(std::string const &stringParam);

    /*!
     * Wrapper for the getString method
     *
     * Java prototype:
     * `public abstract java.lang.String getString(int);`
     *
     * JNI signature: (I)Ljava/lang/String;
     *
     */
    std::string getString(int32_t column);

    /*!
     * Wrapper for the getInt method
     *
     * Java prototype:
     * `public abstract int getInt(int);`
     *
     * JNI signature: (I)I
     *
     */
    int32_t getInt(int32_t column);

    /*!
     * Class metadata
     */
    struct Meta : public MetaBaseDroppable {
        jni::method_t getCount;
        jni::method_t moveToFirst;
        jni::method_t getColumnIndex;
        jni::method_t getString;
        jni::method_t getInt;

        /*!
         * Singleton accessor
         */
        static Meta &data() {
            static Meta instance;
            return instance;
        }

      private:
        Meta();
    };
};
} // namespace android::database
} // namespace wrap
#include "android.database.impl.h"
