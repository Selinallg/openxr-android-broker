// Copyright 2020, Collabora, Ltd.
// SPDX-License-Identifier: BSL-1.0
// Author: Ryan Pavlik <ryan.pavlik@collabora.com>

#pragma once

#include <string>

namespace wrap {
namespace android::database {
inline int32_t Cursor::getCount() {
    assert(!isNull());
    return object().call<int32_t>(Meta::data().getCount);
}

inline bool Cursor::moveToFirst() {
    assert(!isNull());
    return object().call<bool>(Meta::data().moveToFirst);
}

inline int32_t Cursor::getColumnIndex(std::string const &columnName) {
    assert(!isNull());
    return object().call<int32_t>(Meta::data().getColumnIndex, columnName);
}

inline std::string Cursor::getString(int32_t column) {
    assert(!isNull());
    return object().call<std::string>(Meta::data().getString, column);
}

inline int32_t Cursor::getInt(int32_t column) {
    assert(!isNull());
    return object().call<int32_t>(Meta::data().getInt, column);
}
} // namespace android::database
} // namespace wrap
