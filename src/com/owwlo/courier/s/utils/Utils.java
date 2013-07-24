package com.owwlo.courier.s.utils;

import java.util.ArrayList;
import java.util.Random;

import com.owwlo.courier.s.data.Contact;
import com.owwlo.courier.s.data.MessageItem;
import com.owwlo.courier.s.Constants;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.text.TextUtils;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static String byteArrayToHexString(byte[] array) {
        StringBuffer hexString = new StringBuffer();
        for (byte b : array) {
            int intVal = b & 0xff;
            if (intVal < 0x10)
                hexString.append("0");
            hexString.append(Integer.toHexString(intVal));
        }
        return hexString.toString();
    }

    public static char generateRandomAuthChar() {
        Random random = new Random();
        int pickIndex = random.nextInt(36);
        if (pickIndex < 10) {
            return (char) ('0' + pickIndex);
        } else {
            return (char) ('A' + pickIndex - 10);
        }
    }

    public static boolean isLocalNetConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null
                && (networkInfo.getType() == ConnectivityManager.TYPE_BLUETOOTH
                        || networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET || networkInfo
                        .getType() == ConnectivityManager.TYPE_WIFI);
    }

    public static void sendMessage(Context context, MessageItem item) {
        // 这种方法不能在系统短信中查到，需要手动向Provider里写
        SmsManager sms = SmsManager.getDefault();

        if (item.getBody().length() > 70) {
            ArrayList<String> msgs = sms.divideMessage(item.getBody());
            for (String msg : msgs) {
                sms.sendTextMessage(item.getAddress(), null, msg, null, null);
            }
        } else {
            sms.sendTextMessage(item.getAddress(), null, item.getBody(), null,
                    null);
        }

        // 将短信数据写入系统短信数据库
        ContentValues values = new ContentValues();
        values.put("date", System.currentTimeMillis());
        values.put("read", 0);
        values.put("type", 2);
        values.put("address", item.getAddress());
        values.put("body", item.getBody());
        context.getContentResolver().insert(Uri.parse("content://sms"),values);
    }

    /**
     * 返回的Contact可能为空，号码为空
     */
    public static Contact getContactInfoFormPhone(Context mContext, String number) {
        Contact contact = new Contact();

        String minMatch = PhoneNumberUtils.toCallerIDMinMatch(number);
        if (!TextUtils.isEmpty(number) && !TextUtils.isEmpty(minMatch)) {
            Cursor cursor = mContext.getContentResolver().query(
                    Constants.PHONES_WITH_PRESENCE_URI, Constants.CALLER_ID_PROJECTION,
                    Constants.CALLER_ID_SELECTION, new String[] { minMatch },
                    "length(" + Phone.NUMBER + ")");
            if (cursor != null) {
                try {
                    cursor.moveToPosition(-1);
                    while (cursor.moveToNext()) {
                        String numberInDb = cursor.getString(Constants.PHONE_NUMBER_COLUMN);
                        if (PhoneNumberUtils.compare(number, numberInDb)) {
                            fillPhoneTypeContact(contact, cursor);
                            return contact;
                        }
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return contact;
    }

    private static void fillPhoneTypeContact(final Contact contact, final Cursor cursor) {
            contact.mName = cursor.getString(Constants.CONTACT_NAME_COLUMN);
            contact.mPhotoId = cursor.getLong(Constants.CONTACT_PHOTO_ID_COLUMN);
            contact.mPersonId = cursor.getLong(Constants.CONTACT_ID_COLUMN);
    }
}
