package com.owwlo.courier.s.poster;

import android.os.Message;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;

public abstract class Poster implements Runnable {
    private LinkedList<PosterListener> mPosterListeners = new LinkedList<PosterListener>();

    protected void notifyOnReceiveMessageFromClient(Socket paramSocket,
            Message paramMessage) {
        Iterator<PosterListener> localIterator = mPosterListeners.iterator();
        while (true) {
            if (!localIterator.hasNext())
                return;
            ((PosterListener) localIterator.next()).OnReceiveMessageFromClient(
                    paramSocket, paramMessage);
        }
    }

    protected void notifyOnLastClientExit() {
        for (PosterListener listener : mPosterListeners) {
            listener.onLastClientExit();
        }
    }

    public abstract int getConnectedCount();

    public void addPosterListener(PosterListener paramPosterListener) {
        mPosterListeners.add(paramPosterListener);
    }

    public abstract void init();

    public void removePosterListener(PosterListener paramPosterListener) {
        mPosterListeners.remove(paramPosterListener);
    }

    public abstract boolean sendMessage(Message paramMessage);

    public static abstract interface PosterListener {
        public abstract void OnReceiveMessageFromClient(Socket paramSocket,
                Message paramMessage);
        public abstract void onLastClientExit();
    }
}