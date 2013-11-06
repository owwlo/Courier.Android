package com.owwlo.courier.s.data;

import java.util.LinkedList;
import java.util.List;

public class MessagePack {
    public long senderThreadId;
    public String senderAddress;
    public String senderId;
    private List<MessageItem> messageArray;

    public MessagePack() {
        messageArray = new LinkedList<MessageItem>();
    }

    public final List<MessageItem> getMessageList() {
        return messageArray;
    }

    public void addMessage(MessageItem msg) {
        messageArray.add(msg);
    }
}
