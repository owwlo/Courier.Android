package com.owwlo.courier.s;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootUpReceiver extends BroadcastReceiver {
    private static final String TAG = BootUpReceiver.class.getSimpleName();

    public void onReceive(Context paramContext, Intent paramIntent) {
        paramContext.startService(new Intent(paramContext,
                CourierSService.class));
        Log.i(TAG, "Trigger BootUp receiver.");
    }
}