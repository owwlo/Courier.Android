package com.owwlo.courier.s;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.owwlo.courier.db.CourierDatabaseHelper.ERROR_DETECT;
import com.owwlo.courier.s.Constants.SMS;
import com.owwlo.courier.s.poster.MessagePosterManager;
import com.owwlo.courier.s.utils.Utils;

public class CourierSService extends Service {
    private static final String TAG = "CourierSService";

    public static Context sContext;

    private ServiceBinder mBinder = new ServiceBinder();
    private Handler mHandler = new Handler();
    private ContentObserver mObserver;
    private Timer mBroadcastTimer = new Timer();
    private TimerTask mBroadcastTimerTask = new TimerTask() {
        @Override
        public void run() {
            if (mMessagePosterManager.isConnectedToHost()) {
                mBroadcastTimer.cancel();
                return;
            }
            (new BroadCastHelloMessage(BroadCastHelloMessage.TYPE_KNOCKDOOR))
                    .execute((Void[]) null);
        }
    };

    private MessagePosterManager mMessagePosterManager;
    private InetAddress mLastAddress = null;

    private static String AUTH_CODE;

    private List<AuthcodeListener> mAuthcodeListeners =
            Collections.synchronizedList(new ArrayList<AuthcodeListener>());

    public static final String EXTRA_ACTION = "extra_action";
    public static final String BROADCAST_HELLO_MESSAGE = "broadcast_hello_message";

    public CourierSService() {
        Log.i(TAG, "class loaded.");
        MessagePosterManager.init(this);
        mMessagePosterManager = MessagePosterManager.getInstance();
    }

    private void addSMSObserver() {
        sContext = getApplicationContext();
        Log.i("CourierSService", "add a SMS observer. ");
        ContentResolver localContentResolver = getContentResolver();
        mObserver = new SMSObserver(getApplicationContext(),
                localContentResolver, new SMSHandler(this));
        localContentResolver.registerContentObserver(SMS.CONTENT_URI, true,
                mObserver);
    }

    public final String getAuthCode() {
        return AUTH_CODE;
    }

    public String getSystemTime() {
        Time localTime = new Time();
        localTime.setToNow();
        return localTime.toString();
    }

    public IBinder onBind(Intent paramIntent) {
        Log.i("CourierSService", "start IBinder~~~");
        return mBinder;
    }

    public void onCreate() {
        Log.i("CourierSService", "start onCreate~~~");
    }

    private boolean checkLastSession() {
        Cursor cursor = sContext.getContentResolver().query(
                Constants.URI_LAST_CONNECT, null, null, null,
                ERROR_DETECT.CONNECT_TIME + " desc limit 1");
        if (cursor.getCount() < 1) {
            return false;
        }
        cursor.moveToFirst();
        final String ip = cursor.getString(cursor
                .getColumnIndex(ERROR_DETECT.IP));
        sContext.getContentResolver().delete(Constants.URI_LAST_CONNECT, null,
                null);

        cursor.close();

        if (mMessagePosterManager.isConnectedToHost()) {
            mBroadcastTimer.cancel();
            return false;
        }

        (new BroadCastHelloMessage(BroadCastHelloMessage.TYPE_RECONNECT, ip))
                .execute((Void[]) null);
        return true;
    }

    public boolean onUnbind(Intent paramIntent) {
        return super.onUnbind(paramIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "On receive command.");

        addSMSObserver();

        if (Utils.isLocalNetConnected(this)
                && !mMessagePosterManager.isConnectedToHost()) {
            if (mLastAddress == null
                    || mLastAddress != getLocalIPAddress().get(0)) {
                generateAuthCode();
            }

            boolean isConnectLost = checkLastSession();

            try {
                mBroadcastTimer.scheduleAtFixedRate(mBroadcastTimerTask,
                        isConnectLost ? Constants.RECONNECT_TRY_VALID_TIME : 0,
                        Constants.BROADCAST_DELAY_TIME);
            } catch (Exception e) {
                Log.i(TAG, "schedule failed.");
            }
        }
        return Service.START_STICKY;
    }

    public void generateAuthCode() {
        Log.i(TAG, "Generate AuthCode.");
        InetAddress ip = getLocalIPAddress().get(0);
        byte[] ipBytes = ip.getAddress();
        String ipHex = Utils.byteArrayToHexString(ipBytes);
        String ipPart = ipHex.substring(ipHex.length() - 2);
        String imxi = Utils.getIMXI(sContext);
        if (TextUtils.isEmpty(imxi) || imxi.length() <= 4) {
            imxi = "d92c"; // Magic Code!
        }
        String imxiPart = imxi.substring(imxi.length() - 4, imxi.length() - 2);
        AUTH_CODE = ipPart.substring(0, 1).toUpperCase()
                + Utils.generateRandomAuthChar()
                + ipPart.substring(1, 2).toUpperCase()
                + Utils.generateRandomAuthChar() + imxiPart;
        Log.d(TAG, "AuthCode: " + AUTH_CODE);
        Toast.makeText(getApplicationContext(), AUTH_CODE, Toast.LENGTH_SHORT)
                .show();

        for (AuthcodeListener al : mAuthcodeListeners) {
            al.onAuthcodeChanged(getAuthCode());
        }
    }

    public class ServiceBinder extends Binder {
        public CourierSService getService() {
            return CourierSService.this;
        }
    }

    private class BroadCastHelloMessage extends AsyncTask<Void, Void, Void> {
        public static final int TYPE_KNOCKDOOR = 0;
        public static final int TYPE_RECONNECT = 1;

        private JSONObject sendObj;
        private String mIp;

        @Override
        protected Void doInBackground(Void... params) {
            List<InetAddress> ipList = getLocalIPAddress();
            broadcastMessage(ipList);
            return null;
        }

        public BroadCastHelloMessage(int type) {
            this(type, null);
        }

        public BroadCastHelloMessage(int type, String ip) {
            if (type == TYPE_RECONNECT && ip == null) {
                throw new InvalidParameterException();
            }

            mIp = ip;
            switch (type) {
            case TYPE_KNOCKDOOR:
                sendObj = buildKnockDoorJSON();
                break;
            case TYPE_RECONNECT:
                sendObj = buildReconnectJSON();
                break;
            }
        }

        private void broadcastMessage(List<InetAddress> ipList) {
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket();
                socket.setBroadcast(true);
                socket.setReuseAddress(true);
                String message = Constants.COURIER_JSON_HEADER + sendObj;
                byte[] data = message.getBytes("UTF-8");
                Log.i(TAG, "Broadcast Message: " + message);
                String base64 = Base64.encodeToString(data, Base64.DEFAULT);
                DatagramPacket packet = new DatagramPacket(base64.getBytes(),
                        base64.getBytes().length);
                InetAddress broadcastAddr = InetAddress
                        .getByName("255.255.255.255");
                packet.setAddress(broadcastAddr);
                packet.setPort(Constants.BROADCAST_PORT);
                socket.send(packet);
                Log.d(TAG, "Hello Message Broadcasted.");
            } catch (SocketException e) {
                Log.d(TAG, "Broadcast socket failed.");
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.d(TAG, "Broadcast packet sent failed.");
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
        }

        private JSONObject buildKnockDoorJSON() {
            int port = mMessagePosterManager.getTcpListeningPort();
            String imxi = Utils.getIMXI(sContext); // imxi could be ""
            JSONObject json = new JSONObject();
            try {
                json.put(Constants.JSON_TYPE, Constants.JSON_TYPE_KNOCKDOOR);
                json.put(Constants.JSON_PORT, port);
                json.put(Constants.JSON_IMXI, imxi);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json;
        }

        private JSONObject buildReconnectJSON() {
            int port = mMessagePosterManager.getTcpListeningPort();
            String imxi = Utils.getIMXI(sContext); // imxi could be ""
            JSONObject json = new JSONObject();
            try {
                json.put(Constants.JSON_TYPE, Constants.JSON_TYPE_RECONNECT);
                // 这种情况下mIp不为空
                json.put(Constants.JSON_RECONNECT_IP, mIp);
                json.put(Constants.JSON_PORT, port);
                json.put(Constants.JSON_IMXI, imxi);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json;
        }
    }

    private ArrayList<InetAddress> getLocalIPAddress() {
        ArrayList<InetAddress> ipList = new ArrayList<InetAddress>();
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() // TODO 暂时只支持IPv4协议
                            && inetAddress instanceof Inet4Address) {
                        ipList.add(inetAddress);
                    }
                }
            }
        } catch (SocketException ex) {
            Log.i(TAG, ex.toString());
        }
        return ipList;
    }

    public static interface AuthcodeListener {
        public void onAuthcodeChanged(String newAuthcode);
    }

    public void addAuthcodeListener(AuthcodeListener al) {
        mAuthcodeListeners.add(al);
    }

    public void removeAuthcodeListener(AuthcodeListener al) {
        mAuthcodeListeners.remove(al);
    }
}