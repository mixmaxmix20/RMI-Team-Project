import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private String messageId;
    private String senderUsername;
    private String content;
    private LocalDateTime timestamp;
    private String chatRoomId;

    public Message(String chatRoomId, String content, String messageId, String senderUsername, LocalDateTime timestamp) {
        this.chatRoomId = chatRoomId;
        this.content = content;
        this.messageId = messageId;
        this.senderUsername = senderUsername;
        this.timestamp = timestamp;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
