// Copyright 2020, Collabora, Ltd.
// SPDX-License-Identifier: BSL-1.0
package org.khronos.openxr.broker_lib;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.common.truth.Truth;

import org.junit.Test;
import org.khronos.openxr.runtime_broker.utils.BrokerContract;

import static com.google.common.truth.Truth.assertThat;


public class ParsedBrokerUriTest {

    private static final int majorVer = 1;
    private static final String packageName = "org.khronos.example";


    @Test
    public void parse() {
        // Check the active runtime URIs
        {
            BrokerUriParser parser = new RuntimeBrokerUriParser();
            Uri activeRuntimeItemUri = BrokerContract.ActiveRuntime.makeContentUri(
                    BrokerContract.BrokerType.RuntimeBroker,
                    majorVer, null);
            Truth.assertThat(parser.parse(activeRuntimeItemUri))
                    .isNotNull();
            ParsedBrokerUri activeRuntimeItem = parser.parse(activeRuntimeItemUri);
            assertThat(activeRuntimeItem).isNotNull();
            assertThat(activeRuntimeItem.isDir()).isFalse();
            assertThat(activeRuntimeItem.row).isNotNull();
            assertThat(activeRuntimeItem.row).isEqualTo(0);
            assertContents(activeRuntimeItem);

            // make sure we check that the authority matters.
            Truth.assertThat((new SystemRuntimeBrokerUriParser()).parse(activeRuntimeItemUri))
                    .isNull();
        }

        // Check the functions URIs
        {
            BrokerUriParser parser = new RuntimeBrokerUriParser();
            Uri functionsDirUri = BrokerContract.Functions.makeContentUri(
                    BrokerContract.BrokerType.RuntimeBroker,
                    majorVer, packageName, null);
            Truth.assertThat(parser.parse(functionsDirUri))
                    .isNotNull();
            ParsedBrokerUri functionsDir = parser.parse(functionsDirUri);
            assertThat(functionsDir).isNotNull();
            assertThat(functionsDir.isDir()).isTrue();
            assertThat(functionsDir.row).isNull();
            assertContents(functionsDir);

            // make sure we check that the authority matters.
            Truth.assertThat((new SystemRuntimeBrokerUriParser()).parse(functionsDirUri))
                    .isNull();
        }
    }

    private void assertContents(@NonNull ParsedBrokerUri parsedBrokerUri) {
        if (parsedBrokerUri.tableType == TableType.ActiveRuntime) {
            Truth.assertThat(parsedBrokerUri.packageName).isNull();
        } else {
            Truth.assertThat(parsedBrokerUri.packageName).isNotNull();
            Truth.assertThat(parsedBrokerUri.packageName).isNotEmpty();
        }
        Truth.assertThat(parsedBrokerUri.majorVer).isEqualTo(majorVer);
    }
}