package com.owwlo.courier.s.data;

import java.io.Serializable;

import com.owwlo.courier.s.CourierSApp;
import com.owwlo.courier.s.CourierSService;
import com.owwlo.courier.s.utils.Utils;

import android.graphics.Bitmap;

public class MessageItem implements Serializable {
    private String address;
    private String body;
    private long date;
    private long dateSent;
    private int deleted;
    private long id;
    private int protocol;
    private int read;
    private int seen;
    private int status;
    private int threadId;
    private int type;
    private Bitmap userImage = null;
    private Contact contact = null;

    public Contact getContact() {
        if (contact == null) {
            contact = Utils.getContactInfoFormPhone(CourierSService.sContext, address);
        }
        return contact;
    }

    public Bitmap getUserImage() {
        return userImage;
    }

    public void setUserImage(Bitmap userImage) {
        this.userImage = userImage;
    }

    public String toString() {
        return "MessageItem [id=" + id + ", threadId=" + threadId
                + ", address=" + address + ", date=" + date + ", dateSent="
                + dateSent + ", read=" + read + ", status=" + status
                + ", type=" + type + ", protocol=" + protocol + ", body="
                + body + ", seen=" + seen + ", deleted=" + deleted + "]";
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getDateSent() {
        return dateSent;
    }

    public void setDateSent(long dateSent) {
        this.dateSent = dateSent;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public int getRead() {
        return read;
    }

    public void setRead(int read) {
        this.read = read;
    }

    public int getSeen() {
        return seen;
    }

    public void setSeen(int seen) {
        this.seen = seen;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}