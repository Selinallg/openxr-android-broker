// Copyright 2020, Collabora, Ltd.
// SPDX-License-Identifier: BSL-1.0
package org.khronos.openxr.runtime_broker

import org.khronos.openxr.broker_lib.AbstractRuntimeBroker
import org.khronos.openxr.broker_lib.BrokerUriParser
import org.khronos.openxr.broker_lib.RuntimeBrokerUriParser
import org.khronos.openxr.runtime_broker.utils.RuntimeChooser

class InstallableRuntimeBroker : AbstractRuntimeBroker() {
    override val runtimeChooser: RuntimeChooser = NoChoiceRuntimeChooser()
    override val parser: BrokerUriParser = RuntimeBrokerUriParser()
}