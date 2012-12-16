package com.owwlo.courier.s;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.owwlo.courier.s.data.MessageItem;
import com.owwlo.courier.s.poster.MessagePosterManager;

public class SMSHandler extends Handler {
    public static final String TAG = SMSHandler.class.getName();
    private Context mContext;
    private MessagePosterManager sMessagePosterManager = MessagePosterManager.getInstance();

    public SMSHandler(Context paramContext) {
        mContext = paramContext;
    }

    public void handleMessage(Message paramMessage) {
        Log.i(TAG, "handleMessage: " + paramMessage);
        sMessagePosterManager.sendMessage(paramMessage);
    }
}
