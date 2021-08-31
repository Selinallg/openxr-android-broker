// Copyright 2021, Qualcomm Innovation Center, Inc.
// SPDX-License-Identifier: BSL-1.0
package org.khronos.openxr.runtime_broker

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.khronos.openxr.runtime_broker.data.RuntimeRepository
import org.khronos.openxr.runtime_broker.data.RuntimeRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InstallableRuntimeBrokerModule {

    @Provides
    @Singleton
    fun provideRuntimeRepository(@ApplicationContext context: Context): RuntimeRepository {
        return RuntimeRepositoryImpl(context)
    }
}
