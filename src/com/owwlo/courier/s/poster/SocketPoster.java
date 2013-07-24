package com.owwlo.courier.s.poster;

import android.content.Context;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.owwlo.courier.s.Constants;
import com.owwlo.courier.s.Constants.SMS;
import com.owwlo.courier.s.data.MessageItem;
import com.owwlo.courier.s.utils.Utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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

        private static final int MAX_ENCRYPT_BLOCK = 117;
        private byte[] IV = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        private IvParameterSpec ivSpec = new IvParameterSpec(IV);

        private Context mContext;
        private Socket mSocket;
        private boolean encryptEnabled = false;
        private PrintWriter mSender;
        private PublicKey mPublicKey;
        private SecretKey mAESKey;
        private List<ClientListener> mPosterListener;
        private ClientConnectionState mConnectionState;

        private enum ClientConnectionState {
            Connected, WaitingForPublicKey
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
            // 连接上后第一个初始状态是“已连接”
            mConnectionState = ClientConnectionState.Connected;

            /**
             * TODO 加密通讯需要调试通
             */
            // sendEncryptOption();
        }

        private void sendEncryptOption() {
            JSONObject json = new JSONObject();
            try {
                json.put(Constants.JSON_TYPE, Constants.JSON_TYPE_NEED_ENCRYPT);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendMessageToClient(json);

            // 连接后发送完加密选项后为“等待公钥”状态
            mConnectionState = ClientConnectionState.WaitingForPublicKey;
        }

        private JSONObject getJSON(MessageItem sms) {
            JSONObject json = new JSONObject();
            try {
                json.put(Constants.JSON_TYPE,Constants.JSON_MESSAGE);
                json.put(SMS.ADDRESS, sms.getAddress());
                json.put(SMS.BODY, sms.getBody());
                json.put(SMS.DATE, sms.getDate());
                json.put(SMS.DATE_SENT, sms.getDateSent());
                json.put(SMS.DELETED, sms.getDeleted());
                json.put(SMS.ID, sms.getId());
                json.put(SMS.PROTOCOL, sms.getProtocol());
                json.put(SMS.READ, sms.getRead());
                json.put(SMS.SEEN, sms.getSeen());
                json.put(SMS.STATUS, sms.getStatus());
                json.put(SMS.THREAD_ID, sms.getThreadId());
                json.put(SMS.TYPE, sms.getType());
            } catch (JSONException e) {
                // 不会执行到这里
                e.printStackTrace();
            }
            return json;
        }

        public void run() {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(
                        mSocket.getInputStream()));
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
                if (br != null) {
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
                        Base64.NO_WRAP));
            } catch (UnsupportedEncodingException e) {
                return;
            }
            if (!string.startsWith(Constants.COURIER_JSON_HEADER)) {
                return;
            }
            string = string.substring(Constants.COURIER_JSON_HEADER.length());

            if (encryptEnabled) {
                try {
                    //byte[] enCodeFormat = mAESKey.getEncoded();
                    //SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
                    Cipher cipher = Cipher.getInstance("AES");
                    cipher.init(Cipher.DECRYPT_MODE, mAESKey);
                    byte[] result = cipher.doFinal(string.getBytes("utf-8"));
                    string = new String(result);
                } catch (NoSuchAlgorithmException e) {
                    // 这里应该不会执行到
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    // 这里应该不会执行到
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    // 这里应该不会执行到
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    // 这里应该不会执行到
                    e.printStackTrace();
                }
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

        /*
         * 主要来处理并分发传来的消息功能的函数
         */
        private void jsonProcessor(JSONObject json) throws Exception {
            String type = json.getString(Constants.JSON_TYPE);
            if (mConnectionState == ClientConnectionState.Connected) {
                if (Constants.JSON_TYPE_REPLY_MSG.equalsIgnoreCase(type)) {
                    MessageItem msg = new MessageItem();
                    msg.setAddress(json.getString(SMS.ADDRESS));
                    msg.setBody(json.getString(SMS.BODY));
                    Utils.sendMessage(mContext, msg);
                }
            } else if (mConnectionState == ClientConnectionState.WaitingForPublicKey) {
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
                                Base64.NO_WRAP);
                        X509EncodedKeySpec spec = new X509EncodedKeySpec(
                                keyBytes);
                        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                        mPublicKey = keyFactory.generatePublic(spec);
                        mAESKey = generateAESKey();
                        if (mAESKey == null) {
                            throw new Exception("AESKey generating failed.");
                        }
                        // 将AESKey发送给PC端
                        sendMessageToClient(getSecureKeyJSON());

                        // TODO 这里需要验证是否发送成功

                        // 重新置为连接状态
                        mConnectionState = ClientConnectionState.Connected;
                        encryptEnabled = true;

                        // 测试加密生效否
                        sendTestEncryptMessage();
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
        }

        private void sendTestEncryptMessage() {
            JSONObject json = new JSONObject();
            try {
                json.put("owwlo", "owwlo is owwlo");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendMessageToClient(json);
        }

        private JSONObject getSecureKeyJSON() {
            JSONObject json = new JSONObject();
            //byte[] aesKeyBytes = mAESKey.getEncoded();
            byte[] aesKeyBytes = null;
            try {
                aesKeyBytes = "owwlo".getBytes("utf-8");
            } catch (UnsupportedEncodingException e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }
            Log.d(TAG, "AES key: " + new String(Base64.encode(aesKeyBytes, Base64.NO_WRAP)));
            KeyFactory keyFactory;
            try {
                keyFactory = KeyFactory.getInstance("RSA");
                Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
                cipher.init(Cipher.ENCRYPT_MODE, mPublicKey);
//                int inputLen = aesKeyBytes.length;
//                ByteArrayOutputStream out = new ByteArrayOutputStream();
//                int offSet = 0;
//                byte[] cache;
//                int i = 0;
//                // 对数据分段加密
//                while (inputLen - offSet > 0) {
//                    if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
//                        cache = cipher.doFinal(aesKeyBytes, offSet,
//                                MAX_ENCRYPT_BLOCK);
//                    } else {
//                        cache = cipher.doFinal(aesKeyBytes, offSet, inputLen
//                                - offSet);
//                    }
//                    out.write(cache, 0, cache.length);
//                    i++;
//                    offSet = i * MAX_ENCRYPT_BLOCK;
//                }
//                byte[] encryptedData = out.toByteArray();
//                out.close();
                byte[] encryptedData = cipher.doFinal(aesKeyBytes);
                try {
                    json.put(Constants.JSON_TYPE, Constants.JSON_TYPE_AES_KEY);
                    json.put(
                            Constants.JSON_VALUE,
                            new String(Base64.encode(encryptedData,
                                    Base64.NO_WRAP)));
                } catch (JSONException e) {
                    // 不会执行到这里
                    e.printStackTrace();
                }
            } catch (NoSuchAlgorithmException e1) {
                // 不会执行
                e1.printStackTrace();
            } catch (NoSuchPaddingException e1) {
                // 不会执行
                e1.printStackTrace();
            } catch (InvalidKeyException e1) {
                // 不会执行
                e1.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                // 不会执行
                e.printStackTrace();
            } catch (BadPaddingException e) {
                // 不会执行
                e.printStackTrace();
            }
            return json;
        }

        private SecretKey generateAESKey() {
            KeyGenerator kg;
            try {
                kg = KeyGenerator.getInstance("AES");
                kg.init(256);
                return kg.generateKey();
            } catch (NoSuchAlgorithmException e) {
                // 不会执行到这里
                e.printStackTrace();
            }
            return null;
        }

        public void sendMessageToClient(Message paramMessage) {
            Log.i(TAG, "send Message to Client " + mSocket.toString());
            sendMessageToClient(getJSON((MessageItem) paramMessage.obj));
        }

        public void sendMessageToClient(JSONObject json) {
            Log.d(TAG, "Going to send Json: " + json.toString());
            byte[] bytesTobeSent = null;
            try {
                bytesTobeSent = json.toString().getBytes("utf-8");
            } catch (UnsupportedEncodingException e1) {
                // 不会执行
            }
            if (encryptEnabled) {
                //byte[] enCodeFormat = mAESKey.getEncoded();
                //SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
                Cipher cipher;
                try {
                    cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
                    cipher.init(Cipher.ENCRYPT_MODE, mAESKey, ivSpec);
                    bytesTobeSent = cipher.doFinal(bytesTobeSent);
                } catch (NoSuchAlgorithmException e) {
                    // 不会执行到这里
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    // 不会执行到这里
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    //PKCS5Padding对齐方法防止这种情况产生
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    // 不会执行到这里
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    // 不会执行到这里
                    e.printStackTrace();
                }
            }
            try {
                byte[] headerBytes = Constants.COURIER_JSON_HEADER
                        .getBytes("utf-8");
                byte[] data = new byte[bytesTobeSent.length
                        + headerBytes.length];
                System.arraycopy(headerBytes, 0, data, 0,
                        headerBytes.length);
                System.arraycopy(bytesTobeSent, 0, data, headerBytes.length,
                        bytesTobeSent.length);
                String base64 = Base64.encodeToString(data, Base64.NO_WRAP);
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
