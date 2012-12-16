package com.owwlo.courier.s.poster;

import android.os.Message;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;

public abstract class Poster implements Runnable {
    private LinkedList<PosterListener> mPosterListeners = new LinkedList();

    private void notifyOnReceiveMessageFromClient(Socket paramSocket,
            Message paramMessage) {
        Iterator localIterator = mPosterListeners.iterator();
        while (true) {
            if (!localIterator.hasNext())
                return;
            ((PosterListener) localIterator.next()).OnReceiveMessageFromClient(
                    paramSocket, paramMessage);
        }
    }

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
    }
}

/*
 * Location: /home/owwlo/com.owwlo.courier.s-1.apk_FILES/classes_dex2jar.jar
 * Qualified Name: com.owwlo.courier.s.poster.Poster JD-Core Version: 0.6.2
 */