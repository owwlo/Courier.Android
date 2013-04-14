package com.owwlo.courier.s.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class CourierUtils {
    public static boolean isLocalNetConnected(Context context) {
        ConnectivityManager manager =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null &&
                (networkInfo.getType() == ConnectivityManager.TYPE_BLUETOOTH
                || networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET
                || networkInfo.getType() == ConnectivityManager.TYPE_WIFI);
    }
}
