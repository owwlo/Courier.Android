package com.owwlo.courier.s.poster;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 在CourierSService的OnCreate函数中初始化
 */
public class MessagePosterManager {
    private static final String TAG = "CourierSMessagePosterManager";

    private static MessagePosterManager sMessagePoster;
    private Context mContext;
    private HandlerThread mMessagePosterThread;
    private LinkedList<Poster> mPosterList;
    private int mTcpListenPort;
    private List<PosterManagerListener> mPosterManagerListeners;

    public MessagePosterManager(Context paramContext) {
        mContext = paramContext;
        mPosterList = new LinkedList<Poster>();
        mPosterManagerListeners =
                Collections.synchronizedList(new ArrayList<PosterManagerListener>());
    }

    public static MessagePosterManager getInstance() {
        return sMessagePoster;
    }

    public static void init(Context paramContext) {
        sMessagePoster = new MessagePosterManager(paramContext);
        sMessagePoster.initData();
    }

    private void initData() {
        mTcpListenPort = getIdleTcpPort();

        mMessagePosterThread = new HandlerThread(TAG);
        mMessagePosterThread.start();
        preparePoster(new SocketPoster(mContext));
    }

    // TODO get a free port for host listening
    private int getIdleTcpPort() {
        return 55837;
    }

    public int getTcpListeningPort() {
        return mTcpListenPort;
    }

    private void preparePoster(Poster paramPoster) {
        paramPoster.init();
        paramPoster.addPosterListener(new Poster.PosterListener() {
            public void OnReceiveMessageFromClient(Socket paramAnonymousSocket,
                    Message paramAnonymousMessage) {
                Log.i(MessagePosterManager.TAG, "There is Message Form PC!");
            }

            @Override
            public void onLastClientExit() {
                for (PosterManagerListener listener : mPosterManagerListeners) {
                    listener.onLastClientExit();
                }
            }
        });
        mPosterList.add(paramPoster);
    }

    public void sendMessage(Message msg) {
        for (Poster poster : mPosterList) {
            poster.sendMessage(msg);
        }
    }

    public boolean isConnectedToHost() {
        for (Poster poster : mPosterList) {
            if (poster.getConnectedCount() > 0) {
                return true;
            }
        }
        return false;
    }

    public static interface PosterManagerListener {
        public abstract void onLastClientExit();
    }

    public void addPosterManagerListener(PosterManagerListener listener) {
        mPosterManagerListeners.add(listener);
    }

    public void removePosterManagerListener(PosterManagerListener listener) {
        mPosterManagerListeners.remove(listener);
    }
}