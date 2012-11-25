package com.owwlo.courier.s.poster;

import android.content.Context;
import android.os.Message;
import android.util.Log;
import com.owwlo.courier.s.data.MessageItem;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONObject;

public class SocketPoster extends Poster
{
  private static int SOCKET_LISTENING_PORT = 26942;
  private static final String TAG = SocketPoster.class.getSimpleName();
  private HashMap<Socket, ClientAnswerThread> mClientThreads;
  private Context mContext;
  private ServerSocket mServerSocket;

  public SocketPoster(Context paramContext)
  {
    mContext = paramContext;
    mClientThreads = new HashMap();
    try
    {
      mServerSocket = new ServerSocket(SOCKET_LISTENING_PORT);
      return;
    }
    catch (IOException localIOException)
    {
      while (true)
      {
        Log.e(TAG, "Port " + SOCKET_LISTENING_PORT + " already in using");
        localIOException.printStackTrace();
      }
    }
  }

  public void init()
  {
    new Thread(this).start();
  }

  public void run()
  {
    while (true)
      try
      {
        Socket localSocket = mServerSocket.accept();
        ClientAnswerThread localClientAnswerThread = new ClientAnswerThread(mContext, localSocket);
        localClientAnswerThread.start();
        mClientThreads.put(localSocket, localClientAnswerThread);
        Log.i(TAG, "ConnectIn " + localSocket.toString());
      }
      catch (IOException localIOException)
      {
        localIOException.printStackTrace();
      }
  }

  public boolean sendMessage(Message paramMessage)
  {
    boolean bool;
    if ((mClientThreads == null) || (mClientThreads.size() == 0) || (mServerSocket == null))
    {
      bool = false;
      return bool;
    }
    Iterator localIterator = mClientThreads.values().iterator();
    while (true)
    {
      if (!localIterator.hasNext())
      {
        bool = true;
        break;
      }
      ((ClientAnswerThread)localIterator.next()).sendMessageToClient(paramMessage);
    }
    return bool;
  }

  private class ClientAnswerThread extends Thread
  {
    private final String TAG = ClientAnswerThread.class.getSimpleName();
    private Context mContext;
    private Socket mSocket;

    public ClientAnswerThread(Context paramSocket, Socket arg3)
    {
      mContext = paramSocket;
      mSocket = arg3;
    }

    private JSONObject getJSON(MessageItem paramMessageItem)
    {
      return null;
    }

    public void run()
    {
      try
      {
        BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream(), "UTF-8"));
        StringBuilder localStringBuilder = new StringBuilder();
        while (true)
        {
          if (localBufferedReader.readLine() == null)
          {
            Log.i(TAG, "Receive String: " + localStringBuilder);
            break;
          }
          localStringBuilder.append(localBufferedReader.readLine());
        }
      }
      catch (IOException localIOException)
      {
        localIOException.printStackTrace();
      }
    }

    public void sendMessageToClient(Message paramMessage)
    {
      Log.i(TAG, "send Message to Client " + mSocket.toString());
      getJSON((MessageItem)paramMessage.obj);
    }
  }
}

/* Location:           /home/owwlo/com.owwlo.courier.s-1.apk_FILES/classes_dex2jar.jar
 * Qualified Name:     com.owwlo.courier.s.poster.SocketPoster
 * JD-Core Version:    0.6.2
 */