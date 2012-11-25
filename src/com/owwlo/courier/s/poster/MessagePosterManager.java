package com.owwlo.courier.s.poster;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.net.Socket;

public class MessagePosterManager
{
  private static final String TAG = MessagePosterManager.class.getSimpleName();
  private static MessagePosterManager sMessagePoster;
  private Context mContext;
  private HandlerThread mMessagePosterThread;
  private PosterHandler mPosterHandler;

  public MessagePosterManager(Context paramContext)
  {
    mContext = paramContext;
  }

  public static MessagePosterManager getInstance()
  {
    return sMessagePoster;
  }

  public static void init(Context paramContext)
  {
    sMessagePoster = new MessagePosterManager(paramContext);
    sMessagePoster.initData();
  }

  private void initData()
  {
    mMessagePosterThread = new HandlerThread(TAG);
    mMessagePosterThread.start();
    mPosterHandler = new PosterHandler(mMessagePosterThread.getLooper());
    preparePoster(new SocketPoster(mContext));
  }

  private void preparePoster(Poster paramPoster)
  {
    paramPoster.init();
    paramPoster.addPosterListener(new Poster.PosterListener()
    {
      public void OnReceiveMessageFromClient(Socket paramAnonymousSocket, Message paramAnonymousMessage)
      {
        Log.i(MessagePosterManager.TAG, "There is Message Form PC!");
      }
    });
    mPosterHandler.post(paramPoster);
  }

  private class PosterHandler extends Handler
  {
    public PosterHandler(Looper arg2)
    {
      super();
    }
  }
}

/* Location:           /home/owwlo/com.owwlo.courier.s-1.apk_FILES/classes_dex2jar.jar
 * Qualified Name:     com.owwlo.courier.s.poster.MessagePosterManager
 * JD-Core Version:    0.6.2
 */