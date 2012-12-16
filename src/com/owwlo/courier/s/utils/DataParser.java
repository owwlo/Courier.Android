package com.owwlo.courier.s.utils;

import android.database.Cursor;
import com.owwlo.courier.s.data.MessageItem;

public class DataParser {
    public static final String TAG = DataParser.class.getSimpleName();

    public static MessageItem getMessageItem(Cursor paramCursor) {
        MessageItem localMessageItem = new MessageItem();
        localMessageItem.address = paramCursor.getString(paramCursor
                .getColumnIndex("address"));
        localMessageItem.body = paramCursor.getString(paramCursor
                .getColumnIndex("body"));
        localMessageItem.date = paramCursor.getLong(paramCursor
                .getColumnIndex("date"));
        localMessageItem.dateSent = paramCursor.getLong(paramCursor
                .getColumnIndex("date_sent"));
        localMessageItem.deleted = paramCursor.getInt(paramCursor
                .getColumnIndex("deleted"));
        localMessageItem.id = paramCursor.getInt(paramCursor
                .getColumnIndex("_id"));
        localMessageItem.protocol = paramCursor.getInt(paramCursor
                .getColumnIndex("protocol"));
        localMessageItem.read = paramCursor.getInt(paramCursor
                .getColumnIndex("read"));
        localMessageItem.seen = paramCursor.getInt(paramCursor
                .getColumnIndex("seen"));
        localMessageItem.status = paramCursor.getInt(paramCursor
                .getColumnIndex("status"));
        localMessageItem.threadId = paramCursor.getInt(paramCursor
                .getColumnIndex("thread_id"));
        localMessageItem.type = paramCursor.getInt(paramCursor
                .getColumnIndex("type"));
        return localMessageItem;
    }
}
