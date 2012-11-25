package com.owwlo.courier.s;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootUpReceiver extends BroadcastReceiver
{
  public void onReceive(Context paramContext, Intent paramIntent)
  {
    paramContext.startService(new Intent(paramContext, CourierSService.class));
  }
}

/* Location:           /home/owwlo/com.owwlo.courier.s-1.apk_FILES/classes_dex2jar.jar
 * Qualified Name:     com.owwlo.courier.s.BootUpReceiver
 * JD-Core Version:    0.6.2
 */