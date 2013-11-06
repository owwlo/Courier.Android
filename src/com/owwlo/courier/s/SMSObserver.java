package com.owwlo.courier.s;

import org.json.JSONArray;

import com.owwlo.courier.s.Constants.SMS;
import com.owwlo.courier.s.data.Contact;
import com.owwlo.courier.s.data.MessageItem;
import com.owwlo.courier.s.data.MessagePack;
import com.owwlo.courier.s.utils.Utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

class SMSObserver extends ContentObserver {
    private static final String TAG = "CourierSSMSObserver";

    private static final int SMS_ALL_ID = 0;

    private static final UriMatcher sURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI("sms", "#", SMS_ALL_ID);
    }

    private ContentResolver mResolver;
    private Handler mHandler;
    private Context mContext;

    public SMSObserver(Context context, ContentResolver resolver,
            Handler handler) {
        super(handler);
        mResolver = resolver;
        mHandler = handler;
        mContext = context;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);

        if (sURIMatcher.match(uri) != SMS_ALL_ID) {
            return;
        }

        Log.i(TAG, uri.toString());

        long newID = ContentUris.parseId(uri);

        Cursor cursor = mResolver.query(SMS.CONTENT_URI,
                Constants.SMS_PROJECTION,
                String.format(Constants.SMS_SELECTION, newID), null, null);
        cursor.moveToFirst();
        Log.i(TAG, "" + cursor.getCount());

        // 发送短信会触发，但此时TYPE为送出短信值为2
        if (cursor.getCount() < 1) {
            return;
        }

        do {
            int msgRead = cursor.getInt(cursor.getColumnIndex(SMS.READ));
            int threadId = cursor.getInt(cursor.getColumnIndex(SMS.THREAD_ID));
            String senderAddress = cursor.getString(cursor
                    .getColumnIndex(SMS.ADDRESS));

            // 表示此条短信是程序启动后接到的
            if (msgRead == SMS.MESSAGE_UNREAD
            /*
             * && Math.abs(date - System.currentTimeMillis()) <
             * Constants.SMS_SYSTEM_RESPONSE_ERROR_TIME
             */) {
                Uri conUri = Uri.withAppendedPath(SMS.SMS_CONVERSION, "/"
                        + threadId);
                Cursor threadCursor = mResolver.query(conUri,
                        Constants.SMS_PROJECTION, null, null, SMS.ID
                                + " desc limit " + Constants.THREAD_MAX_FETCH_NUM);

                MessagePack msgPack = new MessagePack();
                msgPack.senderAddress = senderAddress;
                msgPack.senderThreadId = threadId;

                threadCursor.moveToFirst();
                do {
                    int id = threadCursor.getInt(threadCursor
                            .getColumnIndex(SMS.ID));
                    int type = threadCursor.getInt(threadCursor
                            .getColumnIndex(SMS.TYPE));
                    String address = threadCursor.getString(threadCursor
                            .getColumnIndex(SMS.ADDRESS));
                    String body = threadCursor.getString(threadCursor
                            .getColumnIndex(SMS.BODY));
                    long date = threadCursor.getLong(threadCursor
                            .getColumnIndex(SMS.DATE));
                    int read = threadCursor.getInt(threadCursor
                            .getColumnIndex(SMS.READ));
                    long dateSent = threadCursor.getLong(threadCursor
                            .getColumnIndex(SMS.DATE_SENT));
                    int deleted = threadCursor.getInt(threadCursor
                            .getColumnIndex(SMS.DELETED));
                    int protocol = threadCursor.getInt(threadCursor
                            .getColumnIndex(SMS.PROTOCOL));
                    int seen = threadCursor.getInt(threadCursor
                            .getColumnIndex(SMS.SEEN));
                    int status = threadCursor.getInt(threadCursor
                            .getColumnIndex(SMS.STATUS));
                    Log.i(TAG, body + " " + address + " " + threadId);
                    MessageItem item = new MessageItem();
                    if (SMS.MESSAGE_TYPE_INBOX == type) {
                        item.setAddress(address);
                    }
                    item.setBody(body);
                    item.setDate(date);
                    item.setDateSent(dateSent);
                    item.setDeleted(deleted);
                    item.setId(id);
                    item.setProtocol(protocol);
                    item.setRead(read);
                    item.setSeen(seen);
                    item.setStatus(status);
                    item.setThreadId(threadId);
                    item.setType(type);

                    msgPack.addMessage(item);

                    Log.d(TAG, item.toString());

                } while (threadCursor.moveToNext());

                threadCursor.close();

                Message message = new Message();
                message.obj = msgPack;
                mHandler.sendMessage(message);
            }
        } while (cursor.moveToNext());
        cursor.close();
    }
}