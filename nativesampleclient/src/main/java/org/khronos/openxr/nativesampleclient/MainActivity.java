// Copyright 2020-2021, Collabora, Ltd.
// SPDX-License-Identifier: BSL-1.0

package org.khronos.openxr.nativesampleclient;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "NativeSampleClient";

    static {
        System.loadLibrary("nativelib");
    }

    @NonNull
    native static String getRuntime(Context context);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        TextView textView = findViewById(R.id.textBox);
        String msg;
        try {
            msg = getRuntime(this);
        } catch (Exception e) {
            msg = "Caught exception: " + e;
            Log.w(TAG, "Exception!", e);
            return;
        }
        if (msg == null) {
            Log.i(TAG, "Null message!");
            textView.setText("Null message!");
            return;
        }
        Log.i(TAG, "Message: " + msg);
        textView.setText(msg);
    }
}