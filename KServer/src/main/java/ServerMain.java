import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    public static void main(String[] args) {
        try {
            ChatServiceImpl server = new ChatServiceImpl();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("ChatService", server);
            System.out.println("Serwer Chat'u został uruchomiony");
        } catch (RemoteException e) {
            System.err.println("Błąd uruchamiania serwera");
            throw new RuntimeException(e);
        }
    }
}
