import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ChatService extends Remote {
    List<User> clientJoin(ChatObserver client, User user) throws RemoteException;
    void sendMessage(Message message) throws RemoteException;
    void clientLeave(ChatObserver client, User user) throws RemoteException;
    List<Message> getChatHistory(String chatRoomId) throws RemoteException;
    ChatRoom createChatRoom(String roomName, List<String> participants) throws RemoteException;
    ChatRoom getOrCreateDirectMessage(String user1, String user2) throws RemoteException;
    List<ChatRoom> getUserChatRooms(String username) throws RemoteException;
    List<User> getRegisteredUsers() throws RemoteException;
}