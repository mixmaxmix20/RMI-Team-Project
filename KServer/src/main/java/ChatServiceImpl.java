import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class ChatServiceImpl extends UnicastRemoteObject implements ChatService {
    private final ConcurrentHashMap<String, ChatObserver> activeClients;
    private final ConcurrentHashMap<String, ChatRoom> chatRooms;
    private final ConcurrentHashMap<String, List<Message>> roomMessages;
    private final ConcurrentHashMap<String, ReentrantLock> roomLocks;
    private final ConcurrentHashMap<String, User> usersData;

    private final ReentrantLock stateLock = new ReentrantLock(true);

    private AtomicInteger counter = new AtomicInteger(0);

    protected ChatServiceImpl() throws RemoteException {
        super();
        activeClients = new ConcurrentHashMap<>();
        chatRooms = new ConcurrentHashMap<>();
        roomMessages = new ConcurrentHashMap<>();
        roomLocks = new ConcurrentHashMap<>();
        usersData = new ConcurrentHashMap<>();
    }

    @Override
    public List<User> clientJoin(ChatObserver client, User user) throws RemoteException {
        List<User> onlineUsers = new ArrayList<>();
        String username = user.getUsername();

        stateLock.lock();
        try {
            User existingUser = usersData.get(username);
            if (existingUser != null) {
                existingUser.setOnline(true);
            } else {
                user.setOnline(true);
                usersData.put(username, user);
            }
            activeClients.put(username, client);
            System.out.println("Użytkownik " + username + " zalogował się.");

            for (User u : usersData.values()) {
                if (u.isOnline()) {
                    onlineUsers.add(u);
                }
            }

        } finally {
            stateLock.unlock();
        }

        broadcastUserStatus(onlineUsers, username);

        return onlineUsers;
    }

    @Override
    public void sendMessage(Message message) throws RemoteException {
        if (message == null || message.getChatRoomId() == null) {
            System.err.println("Nieprawidłowa wiadomość");
            return;
        }
        String roomId = message.getChatRoomId();
        ChatRoom chatRoom = chatRooms.get(roomId);
        if (chatRoom == null) {
            System.err.println("Pokój nieistnieje");
            return;
        }

        ReentrantLock roomLock = roomLocks.computeIfAbsent(roomId, k -> new ReentrantLock(true));
        roomLock.lock();
        try {
            List<Message> history = roomMessages.computeIfAbsent(roomId, k -> new ArrayList<>());
            history.add(message);
        } finally {
            roomLock.unlock();
        }

        broadCastMessage(chatRoom, message);
    }

    @Override
    public void clientLeave(ChatObserver client, User user) throws RemoteException {
        if (user == null) return;
        String username = user.getUsername();
        List<User> onlineUsers = new ArrayList<>();

        stateLock.lock();
        try {
            activeClients.remove(username);
            User savedUser = usersData.get(username);
            if (savedUser != null) {
                savedUser.setOnline(false);
            }

            System.out.println("Użytkownik " + username + " wylogował się.");

            for (User u : usersData.values()) {
                if (u.isOnline()) {
                    onlineUsers.add(u);
                }
            }
        } finally {
            stateLock.unlock();
        }
        broadcastUserStatus(onlineUsers, username);
    }

    @Override
    public List<Message> getChatHistory(String chatRoomId) throws RemoteException {
        ReentrantLock roomLock = roomLocks.computeIfAbsent(chatRoomId,  k -> new ReentrantLock(true));
        roomLock.lock();
        try {
            List<Message> history = roomMessages.get(chatRoomId);
            if (history != null) {
                return new ArrayList<>(history);
            } else {
                return new ArrayList<>();
            }

        } finally {
            roomLock.unlock();
        }
    }

    @Override
    public ChatRoom createChatRoom(String roomName, List<String> participants) throws RemoteException {
        String roomId = String.valueOf(counter.getAndIncrement());

        List<User> participantList = new ArrayList<>();
        for (String uname : participants) {
            participantList.add(usersData.get(uname));
        }

        ChatRoom newRoom = new ChatRoom(roomId, false, roomName, participantList);
        chatRooms.put(roomId, newRoom);
        notifyAboutNewChatRoom(newRoom);
        return newRoom;
    }

    @Override
    public ChatRoom getOrCreateDirectMessage(String user1, String user2) throws RemoteException {
        String firstU = (user1.compareTo(user2) < 0) ? user1 : user2;
        String secondU = (user1.compareTo(user2) < 0) ? user2 : user1;
        String dmId = String.valueOf("DM_" + firstU + "_" + secondU);

        ChatRoom dmRoom  = chatRooms.get(dmId);

        if (dmRoom == null) {
            List<User> participants = Arrays.asList(usersData.get(user1), usersData.get(user2));
            ChatRoom newRoom = new ChatRoom(dmId, true, "DM: " + user1 + " and " + user2, participants);

            ChatRoom existingRoom = chatRooms.putIfAbsent(dmId, newRoom);

            if (existingRoom == null) {
                dmRoom = newRoom;
                notifyAboutNewChatRoom(dmRoom);
            } else  {
                dmRoom = existingRoom;
            }
        }
        return dmRoom;
    }

    @Override
    public List<ChatRoom> getUserChatRooms(String username) throws RemoteException {
        List<ChatRoom> userRooms = new ArrayList<>();

        for (ChatRoom room : chatRooms.values()) {
            for (User participant : room.getParticipants()) {
                if (participant.getUsername().equals(username)) {
                    userRooms.add(room);
                    break;
                }
            }
        }

        return userRooms;
    }

    @Override
    public List<User> getRegisteredUsers() throws RemoteException {
        return new ArrayList<>(usersData.values());
    }

    private void broadCastMessage(ChatRoom room, Message message) throws RemoteException {
        for (User participant : room.getParticipants()) {
            String username = participant.getUsername();
            ChatObserver chatObserver = activeClients.get(username);

            if (chatObserver != null) {
                try {
                    chatObserver.receiveMessage(message);
                } catch (RemoteException e) {
                    System.out.println("Utracono połączenie z klientem: " + username);
                    activeClients.remove(username);
                    User disconnectedUser = usersData.get(username);
                    disconnectedUser.setOnline(false);
                }
            }
        }
    }

    private void broadcastUserStatus(List<User> onlineUsers, String usernameS) throws RemoteException {
        for (Map.Entry<String, ChatObserver> entry : activeClients.entrySet()) {
            String username = entry.getKey();
            if (username.equals(usernameS)) {
                continue;
            }

            ChatObserver observer = entry.getValue();

            try {
                observer.updateOnlineUsers(onlineUsers);
            } catch (RemoteException e) {
                System.err.println("Błąd powiadomienia dla: " + username);
                activeClients.remove(username);
                User u = usersData.get(username);
                u.setOnline(false);
            }
        }
    }

    private void notifyAboutNewChatRoom(ChatRoom chatRoom) throws RemoteException {
        for (User participant : chatRoom.getParticipants()) {
            String username = participant.getUsername();
            ChatObserver chatObserver = activeClients.get(username);

            if (chatObserver != null) {
                try {
                    chatObserver.notifyAddedToChatRoom(chatRoom);
                } catch (RemoteException e) {
                    System.err.println("Błąd powiadomienia dla: " + username);
                    activeClients.remove(username);
                    User disconnectedUser = usersData.get(username);
                    disconnectedUser.setOnline(false);
                }
            }
        }
    }

}
