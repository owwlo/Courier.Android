package com.owwlo.courier.s.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.owwlo.courier.R;
import com.owwlo.courier.s.data.Contact;
import com.owwlo.courier.s.data.MessageItem;
import com.owwlo.courier.s.Constants;
import com.owwlo.courier.s.CourierSService;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

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
        context.getContentResolver().insert(Uri.parse("content://sms"), values);
    }

    /**
     * 返回的Contact可能为空，号码为空
     */
    public static Contact getContactInfoFormPhone(Context mContext,
            String number) {
        Contact contact = new Contact();
        String minMatch = PhoneNumberUtils.toCallerIDMinMatch(number);
        if (!TextUtils.isEmpty(number) && !TextUtils.isEmpty(minMatch)) {
            Cursor cursor = mContext.getContentResolver().query(
                    Constants.PHONES_WITH_PRESENCE_URI,
                    Constants.CALLER_ID_PROJECTION,
                    Constants.CALLER_ID_SELECTION, new String[] { minMatch },
                    "length(" + Phone.NUMBER + ")");
            if (cursor != null) {
                try {
                    cursor.moveToPosition(-1);
                    while (cursor.moveToNext()) {
                        String numberInDb = cursor
                                .getString(Constants.PHONE_NUMBER_COLUMN);
                        if (PhoneNumberUtils.compare(number, numberInDb)) {
                            getContactFormCursor(contact, cursor);
                        }
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return contact;
    }

    /**
     * needLoadPhones设置为True在结果中载入通讯录中所有的号码，很慢……很慢……慢……
     */
    public static List<Contact> loadAllContacts(Context mContext,
            boolean needLoadPhones) {
        List<Contact> list = new LinkedList<Contact>();
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                int idColumn = cursor
                        .getColumnIndex(ContactsContract.Contacts._ID);
                int displayNameColumn = cursor
                        .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                int photoIdColumn = cursor
                        .getColumnIndex(ContactsContract.Contacts.PHOTO_ID);
                int hasPhoneNumberColumn = cursor
                        .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
                do {
                    Contact contact = new Contact();
                    long contactId = cursor.getLong(idColumn);
                    String displayName = cursor.getString(displayNameColumn);
                    int phoneCount = cursor.getInt(hasPhoneNumberColumn);
                    long photoId = cursor.getLong(photoIdColumn);
                    contact.mName = displayName;
                    contact.mPersonId = contactId;
                    contact.mPhotoId = photoId;
                    if (needLoadPhones) {
                        if (phoneCount > 0) {
                            Cursor phoneCursor = mContext
                                    .getContentResolver()
                                    .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                            null,
                                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                                    + "=" + contactId, null,
                                            null);
                            try {
                                if (phoneCursor.moveToFirst()) {
                                    do {
                                        String phoneNumber = phoneCursor
                                                .getString(phoneCursor
                                                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                        contact.addPhone(phoneNumber);
                                    } while (phoneCursor.moveToNext());
                                }
                            } finally {
                                phoneCursor.close();
                            }
                        } else {
                            continue;
                        }
                    }
                    list.add(contact);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    private static void getContactFormCursor(final Contact contact,
            final Cursor cursor) {
        contact.mName = cursor.getString(Constants.CONTACT_NAME_COLUMN);
        contact.mPhotoId = cursor.getLong(Constants.CONTACT_PHOTO_ID_COLUMN);
        contact.mPersonId = cursor.getLong(Constants.CONTACT_ID_COLUMN);
    }

    public static String encodeImageTobase64(Bitmap image) {
        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.PNG, 0, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.NO_WRAP);
        return imageEncoded;
    }

    public static Bitmap getUserImageByPersonId(Context context, long personId) {
        Bitmap contactPhoto;
        ContentResolver resolver = context.getContentResolver();
        if (personId > 0) {
            Uri uri = ContentUris.withAppendedId(
                    ContactsContract.Contacts.CONTENT_URI, personId);
            InputStream input = ContactsContract.Contacts
                    .openContactPhotoInputStream(resolver, uri);
            contactPhoto = BitmapFactory.decodeStream(input);
        } else {
            contactPhoto = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.default_contact_photo);
        }
        return contactPhoto;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        // 按照Android官方的介绍，通讯里头像的长宽相当
        // 4.0之前为96px 4.0为256px 4.1为720px
        int resize = bitmap.getHeight() > 96 ? 96 : bitmap.getHeight();

        Bitmap output = Bitmap.createBitmap(resize, resize, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, resize, resize);
        final Rect desRect = new Rect(0, 0, resize, resize);
        final RectF rectF = new RectF(desRect);
        final float roundPx = (float)(resize * Constants.USER_IMAGE_RADIUS_PERCENT);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, desRect, paint);

        return output;
    }

    public static String getIMXI(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getApplicationContext().getSystemService(
                        Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = "";
        }
        return deviceId;
    }
}
