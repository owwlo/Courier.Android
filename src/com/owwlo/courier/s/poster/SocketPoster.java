package com.owwlo.courier.s.poster;

import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.owwlo.courier.s.Constants;
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

public class SocketPoster extends Poster {
    private static final String TAG = "CourierSSocketPoster";

    private HashMap<Socket, ClientAnswerThread> mClientThreads;
    private Context mContext;
    private ServerSocket mServerSocket;

    public SocketPoster(Context paramContext) {
        mContext = paramContext;
        mClientThreads = new HashMap();
        try {
            mServerSocket = new ServerSocket(Constants.SOCKET_LISTENING_PORT);
        } catch (IOException localIOException) {
            Log.w(TAG, "SocketPoster create failed.");
        }
    }

    @Override
    public void init() {
        new Thread(this).start();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mServerSocket.close();
    }

    @Override
    public void run() {
        while (true)
        {
            try {
                Socket localSocket = mServerSocket.accept();
                ClientAnswerThread localClientAnswerThread = new ClientAnswerThread(
                        mContext, localSocket);
                localClientAnswerThread.start();
                mClientThreads.put(localSocket, localClientAnswerThread);
                Log.i(TAG, "ConnectIn " + localSocket.toString());
            } catch (IOException localIOException) {
                Log.w(TAG, "Accept incomming connection Failed.");
            }
        }
    }

    @Override
    public boolean sendMessage(Message paramMessage) {
        if ((mClientThreads == null) || (mClientThreads.size() == 0)
                || (mServerSocket == null)) {
            return false;
        }
        Iterator<ClientAnswerThread> localIterator = mClientThreads.values()
                .iterator();
        while (localIterator.hasNext()) {
            ((ClientAnswerThread) localIterator.next())
                    .sendMessageToClient(paramMessage);
        }
        return true;
    }

    private class ClientAnswerThread extends Thread {
        private final String TAG = "CourierSClientAnswerThread";
        private Context mContext;
        private Socket mSocket;

        public ClientAnswerThread(Context paramSocket, Socket arg3) {
            mContext = paramSocket;
            mSocket = arg3;
        }

        private JSONObject getJSON(MessageItem paramMessageItem) {
            return null;
        }

        public void run() {
            try {
                BufferedReader localBufferedReader = new BufferedReader(
                        new InputStreamReader(mSocket.getInputStream(), "UTF-8"));
                StringBuilder localStringBuilder = new StringBuilder();
                while (true) {
                    if (localBufferedReader.readLine() == null) {
                        Log.i(TAG, "Receive String: " + localStringBuilder);
                        break;
                    }
                    localStringBuilder.append(localBufferedReader.readLine());
                }
            } catch (IOException localIOException) {
                localIOException.printStackTrace();
            }
        }

        public void sendMessageToClient(Message paramMessage) {
            Log.i(TAG, "send Message to Client " + mSocket.toString());
            getJSON((MessageItem) paramMessage.obj);
        }
    }
}
