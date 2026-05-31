import javax.swing.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

public class Client {
    static void main() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                    ChatService chatService = (ChatService) registry.lookup("ChatService");

                    ChatFrame mainFrame = new ChatFrame();
                    ChatController controller = new ChatController(chatService, mainFrame);
                    mainFrame.setVisible(true);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                } catch (NotBoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
