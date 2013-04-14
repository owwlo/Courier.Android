package com.owwlo.courier.s;

import android.net.Uri;
import android.provider.BaseColumns;

public class Constants
{
    public abstract interface SMS extends BaseColumns
    {
      public static final Uri CONTENT_URI 		= Uri.parse("content://sms");
      public static final Uri SMS_URI_INBOX 	= Uri.withAppendedPath(CONTENT_URI, "/index");
      public static final Uri SMS_URI_SENT 		= Uri.withAppendedPath(CONTENT_URI, "/sent");

      public static final String BODY 		= "body";
      public static final String ADDRESS 	= "address";
      public static final String DATE 		= "date";
      public static final String DATE_SENT 	= "date_sent";
      public static final String DELETED 	= "deleted";
      public static final String ID 		= "_id";
      public static final int MESSAGE_READ 			= 1;
      public static final int MESSAGE_TYPE_ALL 		= 0;
      public static final int MESSAGE_TYPE_DRAFT 	= 3;
      public static final int MESSAGE_TYPE_FAILED 	= 5;
      public static final int MESSAGE_TYPE_INBOX 	= 1;
      public static final int MESSAGE_TYPE_OUTBOX 	= 4;
      public static final int MESSAGE_TYPE_QUEUED 	= 6;
      public static final int MESSAGE_TYPE_SENT 	= 2;
      public static final int MESSAGE_UNREAD 		= 0;
      public static final String PERSON 	= "person";
      public static final String PROTOCOL 	= "protocol";
      public static final int PROTOCOL_MMS 	= 1;
      public static final int PROTOCOL_SMS 	= 0;
      public static final String READ 		= "read";
      public static final String SEEN 		= "seen";
      public static final String STATUS 	= "status";
      public static final String THREAD_ID 	= "thread_id";
      public static final String TYPE		= "type";
    }

    public static final String COURIER_JSON_HEADER = "CoUrIeR";

    public static final String JSON_PORT 	= "port";
    public static final String JSON_IMXI 	= "imxi";
    public static final String JSON_TYPE 	= "type";
    public static final String JSON_VALUE 	= "value";

    public static final String JSON_TYPE_NEED_ENCRYPT 	= "NeedEncryptionOption";
    public static final String JSON_TYPE_PUBLIC_KEY 	= "PublicKey";
    public static final String JSON_TYPE_AES_KEY 		= "AESKey";

    public static final long BROADCAST_DELAY_TIME = 10 * 1000;

    public static final int BROADCAST_PORT = 27392;
}
