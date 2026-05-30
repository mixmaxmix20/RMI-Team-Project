import java.io.Serializable;
import java.util.List;

public class ChatRoom implements Serializable {
    private String id;
    private String name;
    private List<User> participants;
    private boolean isDirectMessage;

    public ChatRoom(String id, boolean isDirectMessage, String name, List<User> participants) {
        this.id = id;
        this.isDirectMessage = isDirectMessage;
        this.name = name;
        this.participants = participants;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isDirectMessage() {
        return isDirectMessage;
    }

    public void setDirectMessage(boolean directMessage) {
        isDirectMessage = directMessage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }
}
