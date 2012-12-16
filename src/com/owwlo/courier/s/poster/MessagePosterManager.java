package com.owwlo.courier.s.poster;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.net.Socket;
import java.util.LinkedList;

public class MessagePosterManager {
    private static final String TAG = "CourierSMessagePosterManager";

    private static MessagePosterManager sMessagePoster;
    private Context mContext;
    private HandlerThread mMessagePosterThread;
    private LinkedList<Poster> mPosterList;

    public MessagePosterManager(Context paramContext) {
        mContext = paramContext;
        mPosterList = new LinkedList<Poster>();
    }

    public static MessagePosterManager getInstance() {
        return sMessagePoster;
    }

    public static void init(Context paramContext) {
        sMessagePoster = new MessagePosterManager(paramContext);
        sMessagePoster.initData();
    }

    private void initData() {
        mMessagePosterThread = new HandlerThread(TAG);
        mMessagePosterThread.start();
        preparePoster(new SocketPoster(mContext));
    }

    private void preparePoster(Poster paramPoster) {
        paramPoster.init();
        paramPoster.addPosterListener(new Poster.PosterListener() {
            public void OnReceiveMessageFromClient(Socket paramAnonymousSocket,
                    Message paramAnonymousMessage) {
                Log.i(MessagePosterManager.TAG, "There is Message Form PC!");
            }
        });
        mPosterList.add(paramPoster);
    }

    public void sendMessage(Message msg) {
        for (Poster poster : mPosterList) {
            poster.sendMessage(msg);
        }
    }
}