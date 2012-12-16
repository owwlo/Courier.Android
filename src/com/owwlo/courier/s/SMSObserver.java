package com.owwlo.courier.s;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

class SMSObserver extends ContentObserver {
    private static final String TAG = "CourierSSMSObserver";

    private ContentResolver mResolver;

    public SMSObserver(ContentResolver resolver, Handler handler) {
        super(handler);
        mResolver = resolver;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        Log.i(TAG, uri.toString());
    }
}