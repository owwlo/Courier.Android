package com.owwlo.courier.s.data;

import java.io.Serializable;

public class MessageItem implements Serializable {
    public String address;
    public String body;
    public long date;
    public long dateSent;
    public int deleted;
    public int id;
    public int protocol;
    public int read;
    public int seen;
    public int status;
    public int threadId;
    public int type;

    public String toString() {
        return "MessageItem [id=" + id + ", threadId=" + threadId
                + ", address=" + address + ", date=" + date + ", dateSent="
                + dateSent + ", read=" + read + ", status=" + status
                + ", type=" + type + ", protocol=" + protocol + ", body="
                + body + ", seen=" + seen + ", deleted=" + deleted + "]";
    }
}