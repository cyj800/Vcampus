package model;

import java.time.LocalDateTime;

public class Message {
    private int messageId;
    private String senderId;
    private String senderName;
    private String receiverType; // individual, all, teachers, students, course
    private String receiverId;
    private String receiverName;
    private String messageTitle;
    private String messageContent;
    private String messageType; // normal, system, announcement
    private boolean isRead;
    private LocalDateTime sendTime;
    private LocalDateTime readTime;

    // 构造函数
    public Message() {}

    public Message(String senderId, String senderName, String messageTitle, String messageContent) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.messageTitle = messageTitle;
        this.messageContent = messageContent;
        this.receiverType = "individual";
        this.messageType = "normal";
        this.isRead = false;
        this.sendTime = LocalDateTime.now();
    }

    // Getter和Setter方法
    public int getMessageId() { return messageId; }
    public void setMessageId(int messageId) { this.messageId = messageId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getReceiverType() { return receiverType; }
    public void setReceiverType(String receiverType) { this.receiverType = receiverType; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getMessageTitle() { return messageTitle; }
    public void setMessageTitle(String messageTitle) { this.messageTitle = messageTitle; }

    public String getMessageContent() { return messageContent; }
    public void setMessageContent(String messageContent) { this.messageContent = messageContent; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getSendTime() { return sendTime; }
    public void setSendTime(LocalDateTime sendTime) { this.sendTime = sendTime; }

    public LocalDateTime getReadTime() { return readTime; }
    public void setReadTime(LocalDateTime readTime) { this.readTime = readTime; }

    @Override
    public String toString() {
        return "Message{" +
                "messageId=" + messageId +
                ", senderId='" + senderId + '\'' +
                ", senderName='" + senderName + '\'' +
                ", receiverType='" + receiverType + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", receiverName='" + receiverName + '\'' +
                ", messageTitle='" + messageTitle + '\'' +
                ", messageType='" + messageType + '\'' +
                ", isRead=" + isRead +
                ", sendTime=" + sendTime +
                '}';
    }
}