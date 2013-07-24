package com.owwlo.courier.s.data;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.owwlo.courier.R;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

public class Contact {
    public String mName;
    public long mPhotoId;
    public long mPersonId;
    private List<String> phones;

    public Contact() {
        phones = new ArrayList<String>();
    }
    public final List<String> getPhones() {
        return phones;
    }
    public void addPhone(String phone) {
        phones.add(phone);
    }
    public Bitmap getImage(Context mContext) {
        Bitmap contactPhoto;
        ContentResolver resolver = mContext.getContentResolver();
        if(mPhotoId > 0 && mPersonId > 0) {
            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,mPersonId);
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(resolver, uri);
            contactPhoto = BitmapFactory.decodeStream(input);
        }else {
            contactPhoto = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_contact_photo);
        }
        return contactPhoto;
    }
}
