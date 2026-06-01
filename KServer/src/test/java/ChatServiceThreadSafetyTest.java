import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class ChatServiceThreadSafetyTest {

    private ChatServiceImpl chatService;

    @BeforeEach
    void setUp() throws RemoteException {
        chatService = new ChatServiceImpl();
    }

    @Test
    void testConcurrentJoin() throws RemoteException, InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int index = i;
            executorService.submit(() -> {
                try {
                    latch.await();
                    User user = new User("user" + index);
                    chatService.clientJoin(new MockObserver(), user);
                } catch (RemoteException | InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown();
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();
        assertTrue(completed, "Threads did not complete in time");

        List<User> registeredUsers = chatService.getRegisteredUsers();
        assertEquals(threadCount, registeredUsers.size(), "Should have registered all users");
    }

    @Test
    void testConcurrentSendMessage() throws RemoteException, InterruptedException {
        int threadCount = 20;
        int messagesPerThread = 100;

        for (int i = 0; i < threadCount; i++) {
            chatService.clientJoin(new MockObserver(), new User("user" + i));
        }
        
        List<String> participants = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            participants.add("user" + i);
        }
        
        ChatRoom room = chatService.createChatRoom("Test Room", participants);
        String actualRoomId = room.getId();

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int threadIndex = i;
            executorService.submit(() -> {
                try {
                    latch.await();
                    for (int j = 0; j < messagesPerThread; j++) {
                        Message msg = new Message(actualRoomId, "Message " + j, "user" + threadIndex);
                        chatService.sendMessage(msg);
                    }
                } catch (RemoteException | InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown();
        boolean completed = doneLatch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();
        assertTrue(completed, "Threads did not complete in time");

        List<Message> history = chatService.getChatHistory(actualRoomId);
        assertEquals(threadCount * messagesPerThread, history.size(), "All messages should be in history");
    }

    @Test
    void testConcurrentCreateChatRoom() throws RemoteException, InterruptedException {
        int threadCount = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        Set<String> roomIds = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < threadCount; i++) {
            int index = i;
            executorService.submit(() -> {
                try {
                    latch.await();
                    ChatRoom room = chatService.createChatRoom("Room " + index, Collections.emptyList());
                    roomIds.add(room.getId());
                } catch (RemoteException | InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown();
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();
        assertTrue(completed, "Threads did not complete in time");

        assertEquals(threadCount, roomIds.size(), "All room IDs should be unique");
    }

    @Test
    void testConcurrentGetOrCreateDirectMessage() throws RemoteException, InterruptedException {
        int threadCount = 50;
        String user1 = "alice";
        String user2 = "bob";
        
        chatService.clientJoin(new MockObserver(), new User(user1));
        chatService.clientJoin(new MockObserver(), new User(user2));

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        Set<ChatRoom> rooms = Collections.newSetFromMap(new IdentityHashMap<>());

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    latch.await();
                    ChatRoom room = chatService.getOrCreateDirectMessage(user1, user2);
                    rooms.add(room);
                } catch (RemoteException | InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown();
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();
        assertTrue(completed, "Threads did not complete in time");

        assertEquals(1, rooms.size(), "All threads should have received the same ChatRoom instance");
    }

    private static class MockObserver implements ChatObserver {
        @Override public void receiveMessage(Message message) throws RemoteException {}
        @Override public void updateOnlineUsers(List<User> users) throws RemoteException {}
        @Override public void notifyAddedToChatRoom(ChatRoom chatRoom) throws RemoteException {}
    }
}
