package com.owwlo.courier.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CourierDatabaseHelper extends SQLiteOpenHelper {

    public static class ERROR_DETECT {
        public static String TABLE			= "lost";

        public static String ID				= "id";
        public static String IP				= "imxi";
        public static String CONNECT_TIME	= "connect_time";
    }

    private static CourierDatabaseHelper sInstance = null;

    static final String DATABASE_NAME = "database.db";
    static final int DATABASE_VERSION = 1;

    private Context mContext;

    public CourierDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static synchronized CourierDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CourierDatabaseHelper(context);
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ERROR_DETECT.TABLE + " (" +
                ERROR_DETECT.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ERROR_DETECT.CONNECT_TIME + " INTEGER," +
                ERROR_DETECT.IP + " TEXT" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
