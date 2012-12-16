package com.owwlo.courier.s;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.text.format.Time;
import android.util.Log;

import com.owwlo.courier.s.Constants.SMS;
import com.owwlo.courier.s.poster.MessagePosterManager;

public class CourierSService extends Service {
    private static final String TAG = "CourierSService";
    private ServiceBinder mBinder = new ServiceBinder();
    private Handler mHandler = new Handler();
    private ContentObserver mObserver;

    private void addSMSObserver() {
        Log.i("CourierSService", "add a SMS observer. ");
        ContentResolver localContentResolver = getContentResolver();
        mObserver = new SMSObserver(localContentResolver, new SMSHandler(this));
        localContentResolver.registerContentObserver(SMS.CONTENT_URI, true,
                mObserver);
    }

    public String getSystemTime() {
        Time localTime = new Time();
        localTime.setToNow();
        return localTime.toString();
    }

    public IBinder onBind(Intent paramIntent) {
        Log.e("CourierSService", "start IBinder~~~");
        return mBinder;
    }

    public void onCreate() {
        Log.e("CourierSService", "start onCreate~~~");
        super.onCreate();
        MessagePosterManager.init(this);
        addSMSObserver();
    }

    public void onDestroy() {
        Log.e("CourierSService", "start onDestroy~~~");
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mObserver);
    }

    public void onStart(Intent paramIntent, int paramInt) {
        Log.e("CourierSService", "start onStart~~~");
    }

    public boolean onUnbind(Intent paramIntent) {
        Log.e("CourierSService", "start onUnbind~~~");
        return super.onUnbind(paramIntent);
    }

    public class ServiceBinder extends Binder {
        public CourierSService getService() {
            return CourierSService.this;
        }
    }
}