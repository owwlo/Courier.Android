package com.owwlo.courier.s;

import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;

public class Constants {
    public static final String AUTHORITY = "com.owwlo.courier.db";

    public static final Uri URI_LAST_CONNECT = Uri.parse("content://"
            + AUTHORITY + "/last_connect");

    public static final int THREAD_MAX_FETCH_NUM = 5;

    public static final double USER_IMAGE_RADIUS_PERCENT = 0.15;

    public abstract interface SMS extends BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://sms");
        public static final Uri SMS_CONVERSION = Uri.withAppendedPath(
                CONTENT_URI, "/conversations");
        public static final Uri SMS_URI_INBOX = Uri.withAppendedPath(
                CONTENT_URI, "/index");
        public static final Uri SMS_URI_SENT = Uri.withAppendedPath(
                CONTENT_URI, "/sent");

        public static final String BODY = "body";
        public static final String ADDRESS = "address";
        public static final String DATE = "date";
        public static final String DATE_SENT = "date_sent";
        public static final String DELETED = "deleted";
        public static final String ID = "_id";
        public static final String READ = "read";
        public static final String SEEN = "seen";
        public static final String STATUS = "status";
        public static final String THREAD_ID = "thread_id";
        public static final String TYPE = "type";
        public static final String PERSON = "person";
        public static final String PROTOCOL = "protocol";

        public static final String USER_IMAGE = "user_image";

        public static final int MESSAGE_TYPE_ALL = 0;
        public static final int MESSAGE_TYPE_DRAFT = 3;
        public static final int MESSAGE_TYPE_FAILED = 5;
        public static final int MESSAGE_TYPE_INBOX = 1;
        public static final int MESSAGE_TYPE_OUTBOX = 4;
        public static final int MESSAGE_TYPE_QUEUED = 6;
        public static final int MESSAGE_TYPE_SENT = 2;
        public static final int MESSAGE_READ = 1;
        public static final int MESSAGE_UNREAD = 0;
        public static final int PROTOCOL_MMS = 1;
        public static final int PROTOCOL_SMS = 0;
    }

    public static final String[] SMS_PROJECTION = new String[] { SMS._ID,
            SMS.TYPE, SMS.ADDRESS, SMS.BODY, SMS.DATE, SMS.THREAD_ID, SMS.READ,
            SMS.PROTOCOL, SMS.DATE_SENT, SMS.DELETED, SMS.SEEN, SMS.STATUS,
            SMS.PERSON };

    public static final String[] CALLER_ID_PROJECTION = new String[] {
            Phone._ID, // 0
            Phone.NUMBER, // 1
            Phone.LABEL, // 2
            Phone.DISPLAY_NAME, // 3
            Phone.CONTACT_ID, // 4
            Phone.CONTACT_PRESENCE, // 5
            Phone.CONTACT_STATUS, // 6
            Phone.PHOTO_ID, // 8
            Contacts.SEND_TO_VOICEMAIL, // 10
    };

    public static final int PHONE_NUMBER_COLUMN = 1;
    public static final int CONTACT_NAME_COLUMN = 3;
    public static final int CONTACT_ID_COLUMN = 4;
    public static final int CONTACT_PHOTO_ID_COLUMN = 8;

    public static final String SMS_SELECTION = SMS._ID + " = %s" + " and ("
            + SMS.TYPE + " = " + SMS.MESSAGE_TYPE_INBOX + ")";

    public static final String[] PHONES_PROJECTION = new String[] {
            Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID, Phone.CONTACT_ID };

    public static final String CALLER_ID_SELECTION = " Data._ID IN "
            + " (SELECT data_id FROM phone_lookup WHERE min_match = ?)";

    public static final long SMS_SYSTEM_RESPONSE_ERROR_TIME = 1000 * 5;

    public static final String COURIER_JSON_HEADER = "CoUrIeR";

    public static final String JSON_PORT			= "port";
    public static final String JSON_IMXI			= "imxi";
    public static final String JSON_TYPE			= "json_type";
    public static final String JSON_VALUE			= "value";
    public static final String JSON_RECONNECT_IP	= "reconnect_ip";

    public static final String JSON_MSG_PACK_SENDER		= "senderAddress";
    public static final String JSON_MSG_PACK_SENDER_ID	= "senderId";
    public static final String JSON_MSG_PACK_THREAD_ID	= "threadId";
    public static final String JSON_MSG_PACK_MSG_ARRAY	= "messageArray";
    public static final String JSON_MSG_PACK_IMG_ARRAY	= "imageArray";

    public static final String JSON_TYPE_NEED_ENCRYPT = "NeedEncryptionOption";
    public static final String JSON_TYPE_PUBLIC_KEY = "PublicKey";
    public static final String JSON_TYPE_AES_KEY = "AESKey";
    public static final String JSON_TYPE_REPLY_MSG = "ReplyMsg";
    public static final String JSON_TYPE_KNOCKDOOR = "knockDoor";
    public static final String JSON_TYPE_RECONNECT = "reconnect";

    public static final String JSON_MESSAGE_PACK = "NewTextMessagePack";
    public static final String JSON_MESSAGE = "NewTextMessage";

    public static final long BROADCAST_DELAY_TIME = 10 * 1000;

    public static final int BROADCAST_PORT = 27392;

    public static final Uri PHONES_WITH_PRESENCE_URI = Data.CONTENT_URI;

    public static final long RECONNECT_TRY_VALID_TIME = 5 * 1000;
}
