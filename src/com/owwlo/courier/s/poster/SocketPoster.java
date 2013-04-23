package com.owwlo.courier.s.poster;

import android.content.Context;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.owwlo.courier.s.Constants;
import com.owwlo.courier.s.data.MessageItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.json.JSONException;
import org.json.JSONObject;

public class SocketPoster extends Poster {
    private static final String TAG = "CourierSSocketPoster";

    private Map<Socket, ClientAnswerThread> mClientThreads;
    private Context mContext;
    private ServerSocket mServerSocket;
    private MessagePosterManager mMessagePosterManager = MessagePosterManager
            .getInstance();

    public SocketPoster(Context paramContext) {
        mContext = paramContext;
        mClientThreads = Collections
                .synchronizedMap(new HashMap<Socket, ClientAnswerThread>());
        try {
            mServerSocket = new ServerSocket(
                    mMessagePosterManager.getTcpListeningPort());
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
        while (true) {
            try {
                Socket localSocket = mServerSocket.accept();
                ClientAnswerThread localClientAnswerThread = new ClientAnswerThread(
                        mContext, localSocket);
                localClientAnswerThread.addClientListener(new ClientListener() {

                    @Override
                    public void onClientConnecting() {

                    }

                    @Override
                    public void onClientConnected() {
                    }

                    @Override
                    public void onClientDisconnected() {
                        // TODO 从mClientThreads删除当前Client链接
                        checkIfLastExit();
                    }
                });
                localClientAnswerThread.start();
                mClientThreads.put(localSocket, localClientAnswerThread);
                Log.i(TAG, "ConnectIn " + localSocket.toString());
            } catch (IOException localIOException) {
                Log.w(TAG, "Accept incomming connection Failed.");
            }
        }
    }

    private void checkIfLastExit() {
        if (mClientThreads.values().size() == 0) {
            notifyOnLastClientExit();
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

    public static class ClientAnswerThread extends Thread {
        private final String TAG = "CourierSClientAnswerThread";
        private Context mContext;
        private Socket mSocket;
        private boolean encryptEnabled = false;
        private PrintWriter mSender;
        private PublicKey mPublicKey;
        private SecretKey mAESKey;
        private List<ClientListener> mPosterListener;

        private enum ClientConnectionState {

        }

        public ClientAnswerThread(Context paramSocket, Socket arg3) {
            mContext = paramSocket;
            mSocket = arg3;
            mPosterListener = Collections
                    .synchronizedList(new ArrayList<ClientListener>());
            try {
                mSender = new PrintWriter(mSocket.getOutputStream());
            } catch (IOException e) {
                Log.i(TAG, "failed to get printer from OutputStream.");
                e.printStackTrace();
            }
            sendEncryptOption();
        }

        private void sendEncryptOption() {
            JSONObject json = new JSONObject();
            try {
                json.put(Constants.JSON_TYPE, Constants.JSON_TYPE_NEED_ENCRYPT);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendMessageToClient(json);
        }

        private JSONObject getJSON(MessageItem paramMessageItem) {
            return null;
        }

        public void run() {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                String l;
                while (true) {
                    if ((l = br.readLine()) != null) {
                        Log.i(TAG, "Receive String: " + l);
                        processMessage(l);
                    }
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                if(br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        private void processMessage(String string) {
            try {
                string = new String(Base64.decode(string.getBytes("UTF-8"),
                        Base64.DEFAULT));
            } catch (UnsupportedEncodingException e) {
                return;
            }
            if (!string.startsWith(Constants.COURIER_JSON_HEADER)) {
                return;
            }
            string = string.substring(Constants.COURIER_JSON_HEADER.length());

            if (encryptEnabled) {
                // TODO Decrypt Scope
            }
            JSONObject json;
            try {
                json = new JSONObject(string);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "failed to parse JSON from received data.");
                return;
            }
            try {
                jsonProcessor(json);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "bad JSON format.");
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void jsonProcessor(JSONObject json) throws Exception {
            String type = json.getString(Constants.JSON_TYPE);
            if (Constants.JSON_TYPE_PUBLIC_KEY.equalsIgnoreCase(type)) {
                String publicKeyPEM = json.optString(Constants.JSON_VALUE);
                Log.d(TAG, "Got Public Key in PEM format: " + publicKeyPEM);
                String[] linesArray = publicKeyPEM.split("\n");
                List<String> lines = new LinkedList<String>(
                        Arrays.asList(linesArray));
                if (lines.size() > 1 && lines.get(0).startsWith("-----")
                        && lines.get(lines.size() - 1).startsWith("-----")) {
                    lines.remove(0);
                    lines.remove(lines.size() - 1);
                } else {
                    throw new JSONException("Public Key format wrong.");
                }

                StringBuilder sb = new StringBuilder();
                for (String aLine : lines)
                    sb.append(aLine);
                String keyString = sb.toString();
                Log.d("log", "keyString: " + keyString);
                byte[] keyBytes;
                try {
                    keyBytes = Base64.decode(keyString.getBytes("utf-8"),
                            Base64.DEFAULT);
                    X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    mPublicKey = keyFactory.generatePublic(spec);
                    mAESKey = generateAESKey();
                    if (mAESKey == null) {
                        throw new Exception("AESKey generating failed.");
                    }
                    sendMessageToClient(getSecureKeyJSON());
                } catch (UnsupportedEncodingException e) {
                    // 这里应该不会执行到
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    // 这里应该也不会执行到
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                    throw new JSONException("Public Key format wrong.");
                }
            }
        }

        private JSONObject getSecureKeyJSON() {
            JSONObject json = new JSONObject();
            String keyValue = new String(Base64.encode(
                    mAESKey.getEncoded(), Base64.DEFAULT));
            try {
                json.put(Constants.JSON_TYPE, Constants.JSON_TYPE_AES_KEY);
                json.put(Constants.JSON_VALUE, keyValue);
            } catch (JSONException e) {
                // 不会执行到这里
                e.printStackTrace();
            }
            return json;
        }

        private SecretKey generateAESKey() {
            SecureRandom secureRandom = new SecureRandom();
            KeyGenerator kg;
            try {
                kg = KeyGenerator.getInstance("AES");
                kg.init(256, secureRandom);
                return kg.generateKey();
            } catch (NoSuchAlgorithmException e) {
                // 不会执行到这里
                e.printStackTrace();
            }
            return null;
        }

        public void sendMessageToClient(Message paramMessage) {
            Log.i(TAG, "send Message to Client " + mSocket.toString());
            getJSON((MessageItem) paramMessage.obj);
        }

        public void sendMessageToClient(JSONObject json) {
            Log.d(TAG, "Going to send Json: " + json.toString());
            String strTobeSent = json.toString();
            if (encryptEnabled) {
                // TODO Encrypt Scope
            }
            strTobeSent = Constants.COURIER_JSON_HEADER + strTobeSent;
            byte[] data;
            try {
                data = strTobeSent.getBytes("UTF-8");
                String base64 = Base64.encodeToString(data, Base64.DEFAULT);
                Log.i(TAG, "Base64 String: " + base64);
                mSender.println(base64);
                mSender.flush();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void finalize() throws Throwable {
            mSender.close();
            super.finalize();
        }

        public void addClientListener(ClientListener listener) {
            mPosterListener.add(listener);
        }

        public void removeClientListener(ClientListener listener) {
            mPosterListener.remove(listener);
        }
    }

    public static interface ClientListener {

        public void onClientConnecting();

        public void onClientConnected();

        public void onClientDisconnected();
    }

    @Override
    public int getConnectedCount() {
        return mClientThreads.values().size();
    }
}
