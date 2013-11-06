package com.owwlo.courier.s;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class CourierSApp extends Application {
    private static final String TAG = "CourierApp";

    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "start");
    }
}