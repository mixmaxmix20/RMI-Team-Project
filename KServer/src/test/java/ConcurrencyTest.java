import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrencyTest {

    @Test
    public void testConcurrentLogin() throws Exception {
        ChatServiceImpl server = new ChatServiceImpl();
        int clientCount = 10;

        ExecutorService executor = Executors.newFixedThreadPool(clientCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(clientCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < clientCount; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    User user = new User("User_" + userId);
                    ChatObserver client = new MockChatObserver();
                    server.clientJoin(client, user);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("Błąd logowania dla User_" + userId + ": " + e.getMessage());
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        finishLatch.await();
        executor.shutdown();

        List<User> registered = server.getRegisteredUsers();

        Assertions.assertEquals(clientCount, successCount.get(), "Liczba udanych logowań nie zgadza się z oczekiwaną");
        Assertions.assertEquals(clientCount, registered.size(), "Liczba zarejestrowanych użytkowników na serwerze jest nieprawidłowa");
    }

    @Test
    public void testConcurrentMessages() throws Exception {
        ChatServiceImpl server = new ChatServiceImpl();
        int clientCount = 10;
        int messagesPerClient = 50;

        List<String> usernames = new ArrayList<>();
        for (int i = 0; i < clientCount; i++) {
            String name = "User_" + i;
            usernames.add(name);
            server.clientJoin(new MockChatObserver(), new User(name));
        }

        ChatRoom room = server.createChatRoom("TestRoom", usernames);
        String roomId = room.getId();

        ExecutorService executor = Executors.newFixedThreadPool(clientCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(clientCount);

        for (int i = 0; i < clientCount; i++) {
            final String senderName = "User_" + i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Czekaj na sygnał startu
                    for (int m = 0; m < messagesPerClient; m++) {
                        Message msg = new Message(roomId, "Wiadomość testowa " + m, senderName);
                        server.sendMessage(msg);
                    }
                } catch (Exception e) {
                    System.err.println("Błąd wysyłania przez " + senderName + ": " + e.getMessage());
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        finishLatch.await();
        executor.shutdown();

        List<Message> history = server.getChatHistory(roomId);
        int expectedTotal = clientCount * messagesPerClient;

        Assertions.assertEquals(expectedTotal, history.size(), "Liczba zapisanych wiadomości nie zgadza się z oczekiwaną");
    }

    private static class MockChatObserver implements ChatObserver {
        @Override
        public void receiveMessage(Message message) throws RemoteException {}

        @Override
        public void updateOnlineUsers(List<User> users) throws RemoteException {}

        @Override
        public void notifyAddedToChatRoom(ChatRoom chatRoom) throws RemoteException {}
    }
}
