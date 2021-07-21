// Copyright 2020-2021, Collabora, Ltd.
// SPDX-License-Identifier: BSL-1.0
package org.khronos.openxr.system_runtime_broker

import org.khronos.openxr.broker_lib.AbstractRuntimeBroker
import org.khronos.openxr.broker_lib.BrokerUriParser
import org.khronos.openxr.broker_lib.SystemRuntimeBrokerUriParser
import org.khronos.openxr.runtime_broker.utils.RuntimeChooser

/**
 * The bulk of the Runtime Broker logic is in the base class,
 * with behavior customized by the runtimeChooser specified here.
 */
class SystemRuntimeBroker : AbstractRuntimeBroker() {
    override val runtimeChooser: RuntimeChooser = SystemRuntimeChooser()
    override val parser: BrokerUriParser = SystemRuntimeBrokerUriParser()
}