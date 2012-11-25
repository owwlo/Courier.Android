// INTERNAL ERROR //

/* Location:           /home/owwlo/com.owwlo.courier.s-1.apk_FILES/classes_dex2jar.jar
 * Qualified Name:     com.owwlo.courier.s.SMSObserver
 * JD-Core Version:    0.6.2
 */
package com.owwlo.courier.s;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Handler;

class SMSObserver extends ContentObserver {
	private static final String TAG = SMSObserver.class.getSimpleName();

	private ContentResolver mResolver;

	public SMSObserver(ContentResolver resolver, Handler handler) {
		super(handler);
		mResolver = resolver;
	}
}