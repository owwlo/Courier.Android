package com.owwlo.courier.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import com.owwlo.courier.db.CourierDatabaseHelper.ERROR_DETECT;
import com.owwlo.courier.s.Constants;

public class CourierProvider extends ContentProvider {
    private CourierDatabaseHelper mCourierDatabaseHelper;
    private Context mContext;

    private static final int URI_LAST_CONNECT = 0;
    private static final int URI_LAST_CONNECT_SUB = 1;

    private static final UriMatcher URI_MATCHER = new UriMatcher(
            UriMatcher.NO_MATCH);
    static {
        URI_MATCHER.addURI(Constants.AUTHORITY, "last_connect/*",
                URI_LAST_CONNECT_SUB);
        URI_MATCHER.addURI(Constants.AUTHORITY, "last_connect",
                URI_LAST_CONNECT);
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean onCreate() {
        mCourierDatabaseHelper = CourierDatabaseHelper
                .getInstance(getContext());
        mContext = getContext();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mCourierDatabaseHelper.getReadableDatabase();
        Cursor cursor = null;
        int uriMatch = URI_MATCHER.match(uri);

        switch (uriMatch) {
        case URI_LAST_CONNECT_SUB: {
            String imxi = uri.getLastPathSegment();
            if (TextUtils.isEmpty(imxi)) {
                return null;
            }
            cursor = db.query(ERROR_DETECT.TABLE, projection, ERROR_DETECT.IP
                    + "=?", new String[] { imxi }, null, null, null);
            break;
        }
        case URI_LAST_CONNECT: {
            String table = ERROR_DETECT.TABLE;
            cursor = db.query(table, projection, selection, selectionArgs,
                    null, null, sortOrder);
            break;
        }
        default:
            throw new IllegalArgumentException("Unknown URL " + uri);
        }
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        SQLiteDatabase db = mCourierDatabaseHelper.getWritableDatabase();
        int count = 0;
        final int uriMatch = URI_MATCHER.match(uri);

        switch (uriMatch) {
        case URI_LAST_CONNECT_SUB: {
            count = 1;
            String imxi = uri.getLastPathSegment();
            if (TextUtils.isEmpty(imxi)) {
                return 0;
            }
            db.update(ERROR_DETECT.TABLE, values, ERROR_DETECT.IP + " = ? ",
                    new String[] { String.valueOf(imxi) });
            break;
        }
        default:
            throw new IllegalArgumentException("Unknown URL " + uri);
        }
        return count;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mCourierDatabaseHelper.getWritableDatabase();
        final int uriMatch = URI_MATCHER.match(uri);
        switch (uriMatch) {
        case URI_LAST_CONNECT_SUB: {
            long rowId = db.insert(ERROR_DETECT.TABLE, null, values);
            // 插入的数据不直接返回URI
            return null;
        }
        case URI_LAST_CONNECT: {
            long rowId = db.insert(ERROR_DETECT.TABLE, null, values);
            return null;
        }
        default:
            throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mCourierDatabaseHelper.getWritableDatabase();
        final int uriMatch = URI_MATCHER.match(uri);
        int count = 0;
        switch (uriMatch) {
        case URI_LAST_CONNECT_SUB: {
            String imxi = uri.getLastPathSegment();
            if (TextUtils.isEmpty(imxi)) {
                return 0;
            }
            count = db.delete(ERROR_DETECT.TABLE, ERROR_DETECT.IP + " = ? ",
                    new String[] { String.valueOf(imxi) });
            break;
        }
        case URI_LAST_CONNECT: {
            count = db.delete(ERROR_DETECT.TABLE, selection, selectionArgs);
            break;
        }
        default:
            throw new IllegalArgumentException("Unknown URL " + uri);
        }

        return count;
    }
}
