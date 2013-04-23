package com.owwlo.courier.s;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
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
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Base64;
import android.util.Log;

import com.owwlo.courier.s.Constants.SMS;
import com.owwlo.courier.s.poster.MessagePosterManager;
import com.owwlo.courier.s.utils.Utils;

public class CourierSService extends Service {
    private static final String TAG = "CourierSService";

    private ServiceBinder mBinder = new ServiceBinder();
    private Handler mHandler = new Handler();
    private ContentObserver mObserver;
    private Timer mBroadcastTimer = new Timer();
    private TimerTask mBroadcastTimerTask;
    private MessagePosterManager mMessagePosterManager;
    private InetAddress mLastAddress = null;

    private static String AUTH_CODE;

    public static final String EXTRA_ACTION = "extra_action";
    public static final String BROADCAST_HELLO_MESSAGE = "broadcast_hello_message";

    private void addSMSObserver() {
        Log.i("CourierSService", "add a SMS observer. ");
        ContentResolver localContentResolver = getContentResolver();
        mObserver = new SMSObserver(localContentResolver, new SMSHandler(this));
        localContentResolver.registerContentObserver(SMS.CONTENT_URI, true,
                mObserver);
    }

    public static final String getAuthCode() {
        return AUTH_CODE;
    }

    public String getSystemTime() {
        Time localTime = new Time();
        localTime.setToNow();
        return localTime.toString();
    }

    public IBinder onBind(Intent paramIntent) {
        Log.e("CourierSService", "start IBinder~~~");
        return mBinder;
    }

    public void onCreate() {
        Log.e("CourierSService", "start onCreate~~~");
        super.onCreate();
        MessagePosterManager.init(this);
        mMessagePosterManager = MessagePosterManager.getInstance();
        addSMSObserver();

        mBroadcastTimerTask = new TimerTask(){
            @Override
            public void run() {
                if (mMessagePosterManager.isConnectedToHost()) {
                    mBroadcastTimer.cancel();
                    return;
                }
                (new BroadCastHelloMessage()).execute((Void[]) null);
            }
        };
    }

    public boolean onUnbind(Intent paramIntent) {
        return super.onUnbind(paramIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "On receive command.");
        if (Utils.isLocalNetConnected(this) &&
                !mMessagePosterManager.isConnectedToHost()) {
            if (mLastAddress  == null || mLastAddress != getLocalIPAddress().get(0)) {
                generateAuthCode();
            }
            try{
                mBroadcastTimer.scheduleAtFixedRate(mBroadcastTimerTask,
                        0, Constants.BROADCAST_DELAY_TIME);
            } catch (Exception e) {
                Log.e(TAG, "schedule failed.");
            }
        }
        return Service.START_STICKY;
    }

    private void generateAuthCode() {
        Log.i(TAG, "Generate AuthCode.");
        InetAddress ip = getLocalIPAddress().get(0);
        byte[] ipBytes = ip.getAddress();
        String ipHex = Utils.byteArrayToHexString(ipBytes);
        String ipPart = ipHex.substring(ipHex.length()-2);
        String imxi = getIMXI();
        if (TextUtils.isEmpty(imxi) || imxi.length() <= 4) {
            imxi = "d92c";		// Magic Code!
        }
        String imxiPart = imxi.substring(imxi.length() - 4, imxi.length() - 2);
        AUTH_CODE = ipPart.substring(0, 1).toUpperCase()
                + Utils.generateRandomAuthChar()
                + ipPart.substring(1, 2).toUpperCase()
                + Utils.generateRandomAuthChar()
                + imxiPart;
        Log.d(TAG, "AuthCode: " + AUTH_CODE);
    }

    public class ServiceBinder extends Binder {
        public CourierSService getService() {
            return CourierSService.this;
        }
    }

    private class BroadCastHelloMessage extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            List<InetAddress> ipList = getLocalIPAddress();
            broadcastMessage(ipList);
            return null;
        }

        private void broadcastMessage(List<InetAddress> ipList) {
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket();
                socket.setBroadcast(true);
                socket.setReuseAddress(true);
                String message = Constants.COURIER_JSON_HEADER
                        + buildMessageJSON().toString();
                byte[] data = message.getBytes("UTF-8");
                Log.i(TAG, "Broadcast Message: " + message);
                String base64 = Base64.encodeToString(data, Base64.DEFAULT);
                DatagramPacket packet = new DatagramPacket(base64.getBytes(),
                        base64.getBytes().length);
                InetAddress broadcastAddr = InetAddress.getByName("255.255.255.255");
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
                if(socket != null) {
                    socket.close();
                }
            }
        }

        private JSONObject buildMessageJSON() {
            int port = mMessagePosterManager.getTcpListeningPort();
            String imxi = getIMXI();					//imxi could be ""
            JSONObject json = new JSONObject();
            try {
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
                    if (!inetAddress.isLoopbackAddress()	// TODO 暂时只支持IPv4协议
                            && inetAddress instanceof Inet4Address ) {
                        ipList.add(inetAddress);
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        return ipList;
    }

    private String getIMXI() {
        TelephonyManager tm = (TelephonyManager)
                CourierSService.this.getApplicationContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = "";
        }
        return deviceId;
    }
}