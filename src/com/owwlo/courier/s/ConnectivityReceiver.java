package com.owwlo.courier.s;

import com.owwlo.courier.s.utils.CourierUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ConnectivityReceiver extends BroadcastReceiver {
    private static final String TAG = ConnectivityReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if(CourierUtils.isLocalNetConnected(context)) {
            Intent broadcastIntent = new Intent(context, CourierSService.class);
            broadcastIntent.putExtra(
                    CourierSService.EXTRA_ACTION, CourierSService.BROADCAST_HELLO_MESSAGE);
            context.startService(broadcastIntent);
            Log.i(TAG, "Trigger Connectivity Receiver.");
        }
    }

}
