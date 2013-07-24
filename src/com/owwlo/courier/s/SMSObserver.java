package com.owwlo.courier.s;

import com.owwlo.courier.s.Constants.SMS;
import com.owwlo.courier.s.data.MessageItem;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
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

    public SMSObserver(ContentResolver resolver, Handler handler) {
        super(handler);
        mResolver = resolver;
        mHandler = handler;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);

        if(sURIMatcher.match(uri) != SMS_ALL_ID) {
            return;
        }

        Log.i(TAG, uri.toString());

        long newID = ContentUris.parseId(uri);

        Cursor cursor = mResolver.query(SMS.CONTENT_URI,
                Constants.SMS_PROJECTION,
                String.format(Constants.SMS_SELECTION,newID), null, null);
        cursor.moveToFirst();
        Log.i(TAG, "" + cursor.getCount());

        // 发送短信会触发，但此时TYPE为送出短信值为2
        if(cursor.getCount() < 1) {
            return;
        }

        do {
            int id = cursor.getInt(cursor.getColumnIndex(SMS.ID));
            int type = cursor.getInt(cursor.getColumnIndex(SMS.TYPE));
            String address = cursor.getString(cursor
                    .getColumnIndex(SMS.ADDRESS));
            String body = cursor.getString(cursor.getColumnIndex(SMS.BODY));
            long date = cursor.getLong(cursor.getColumnIndex(SMS.DATE));
            int read = cursor.getInt(cursor.getColumnIndex(SMS.READ));
            long dateSent = cursor
                    .getLong(cursor.getColumnIndex(SMS.DATE_SENT));
            int deleted = cursor.getInt(cursor.getColumnIndex(SMS.DELETED));
            int protocol = cursor.getInt(cursor.getColumnIndex(SMS.PROTOCOL));
            int seen = cursor.getInt(cursor.getColumnIndex(SMS.SEEN));
            int status = cursor.getInt(cursor.getColumnIndex(SMS.STATUS));
            int threadId = cursor.getInt(cursor.getColumnIndex(SMS.THREAD_ID));

            // 表示此条短信是程序启动后接到的
            if (read == SMS.MESSAGE_UNREAD
                    /*&& Math.abs(date - System.currentTimeMillis()) < Constants.SMS_SYSTEM_RESPONSE_ERROR_TIME*/) {
                MessageItem item = new MessageItem();
                item.setAddress(address);
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

                Log.d(TAG, item.toString());

                Message message = new Message();
                message.obj = item;
                mHandler.sendMessage(message);
            }
        } while(cursor.moveToNext());
    }
}