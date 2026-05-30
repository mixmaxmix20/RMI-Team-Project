import java.io.Serializable;

public class Message implements Serializable {
    private String senderUsername;
    private String content;
    private String chatRoomId;

    public Message(String chatRoomId, String content, String senderUsername) {
        this.chatRoomId = chatRoomId;
        this.content = content;
        this.senderUsername = senderUsername;
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

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }
}
