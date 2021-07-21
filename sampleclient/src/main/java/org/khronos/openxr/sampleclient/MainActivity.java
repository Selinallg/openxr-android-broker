// Copyright 2020, Collabora, Ltd.
// SPDX-License-Identifier: BSL-1.0
package org.khronos.openxr.sampleclient;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.khronos.openxr.runtime_broker.utils.BrokerContract;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "SampleClient";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        TextView textView = findViewById(R.id.textBox);
        String msg;
        try {
            msg = lookUpRuntime();
        } catch (Exception e) {
            msg = "Caught exception: " + e;
            textView.setText(msg);
            Log.w(TAG, "Exception!", e);
            return;
        }
        Log.i(TAG, "Message: " + msg);
        textView.setText(msg);
    }

    /**
     * This is essentially what the loader would do to find a runtime.
     */
    @NonNull
    private String lookUpRuntime() {
        final String[] projection = new String[]{
                BrokerContract.ActiveRuntime.Columns._ID,
                BrokerContract.ActiveRuntime.Columns.PACKAGE_NAME,
                BrokerContract.ActiveRuntime.Columns.SO_FILENAME,
                BrokerContract.ActiveRuntime.Columns.HAS_FUNCTIONS,
        };
        Uri uri =
                BrokerContract.ActiveRuntime.makeContentUri(
                        BrokerContract.BrokerType.RuntimeBroker,
                        1, null);
        Log.d(TAG, String.format("URI: %s", uri));
        Cursor cursor = getContentResolver().query(uri,
                projection,
                null,
                null,
                null);

        if (cursor == null) {
            return "Null cursor!";
        }
        if (cursor.getCount() < 1) {
            return "Present but empty cursor!";
        }
        cursor.moveToFirst();

        String result = String.format("Found runtime so %s in package %s - has_functions = %d",
                cursor.getString(cursor.getColumnIndex(BrokerContract.ActiveRuntime.Columns.SO_FILENAME)),
                cursor.getString(cursor.getColumnIndex(BrokerContract.ActiveRuntime.Columns.PACKAGE_NAME)),
                cursor.getInt(cursor.getColumnIndex(BrokerContract.ActiveRuntime.Columns.HAS_FUNCTIONS))
        );
        cursor.close();
        return result;
    }
}