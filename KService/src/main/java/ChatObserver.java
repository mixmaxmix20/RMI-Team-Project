import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ChatObserver extends Remote {
    void receiveMessage(Message message) throws RemoteException;
    void updateOnlineUsers(List<User> users) throws RemoteException;
    void notifyAddedToChatRoom(ChatRoom chatRoom) throws RemoteException;
}
