# Copyright 2020, Collabora, Ltd.
#
# SPDX-License-Identifier: BSL-1.0
if(NOT ANDROID_PLATFORM)
  set(ANDROID_PLATFORM 26)
endif()
cmake_minimum_required(VERSION 3.4.1)
project(NativeSampleClient)

# Needed for NDK features
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED TRUE)

find_library(ANDROID_LOG_LIBRARY log)

# Bundled dependency
if(NOT JNIPP_ROOT)
  set(_JNIPP_ROOT_TEMP ${CMAKE_CURRENT_SOURCE_DIR}/../../../../vendor/jnipp)
  if(NOT EXISTS "${_JNIPP_ROOT_TEMP}/jnipp.cpp")
    set(_JNIPP_ROOT_TEMP "jnipp-NOTFOUND")
  endif()
  set(JNIPP_ROOT
      "${_JNIPP_ROOT_TEMP}"
      CACHE PATH
            "The root directory of the jnipp project - usually a submodule")
endif()

set(JNIWRAP_ROOT ${CMAKE_CURRENT_SOURCE_DIR}/../../../../vendor/android-jni-wrappers)
file(GLOB WRAP_SOURCES ${JNIWRAP_ROOT}/wrap/*.cpp)

if(NOT JNIPP_ROOT)
  message(
    FATAL_ERROR
      "jnipp not found! Either initialize the submodule in vendor/jnipp, or point JNIPP_ROOT to where you cloned https://github.com/rpavlik/jnipp/ branch name 'android'"
  )
endif()

add_library(
  nativelib MODULE
  NativeImpl.cpp
  # Bundled dependency
  ${JNIPP_ROOT}/jnipp.cpp ${WRAP_SOURCES})

target_include_directories(
  nativelib
  PRIVATE "${JNIPP_ROOT}" "${JNIWRAP_ROOT}" "${CMAKE_CURRENT_SOURCE_DIR}"
          "${CMAKE_CURRENT_BINARY_DIR}")
target_link_libraries(nativelib PRIVATE ${ANDROID_LOG_LIBRARY})
