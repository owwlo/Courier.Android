package com.owwlo.courier.s;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.owwlo.courier.s.data.MessageItem;

public class SMSHandler extends Handler
{
  public static final String TAG = SMSHandler.class.getName();
  private Context mContext;

  public SMSHandler(Context paramContext)
  {
    mContext = paramContext;
  }

  public void handleMessage(Message paramMessage)
  {
    Log.i(TAG, "handleMessage: " + paramMessage);
  }
}

/* Location:           /home/owwlo/com.owwlo.courier.s-1.apk_FILES/classes_dex2jar.jar
 * Qualified Name:     com.owwlo.courier.s.SMSHandler
 * JD-Core Version:    0.6.2
 */