import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ChatController extends UnicastRemoteObject implements ChatObserver {
    private final ChatService service;
    private final ChatFrame view;
    private final User user;
    private ChatRoom currentChatroom;

    public ChatController(ChatService service, ChatFrame view) throws RemoteException {
        super();

        this.service = service;
        this.view = view;

        String username = ChatFrame.showUsernameModal(view);
        if (username == null || username.trim().isEmpty()) {
            System.exit(0);
        }
        user = new User(username.trim());

        new Thread(() -> {
            try {
                service.clientJoin(this, user);
                refreshUserChannels();
            } catch (RemoteException e) {
                System.out.println("Błąd połączenia z serwerem: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();

        view.getInputPanel().addSendButtonListener(new SendButtonActionListener());
        view.getChatList().addNewChatroomButtonListener(new NewChatroomButtonActionListener());
        view.getChatList().addNewDMButtonListener(new NewDMButtonActionListener());

        view.getChatList().addListSelectionListener(new ChatListSelectionListener());
    }

    private void refreshUserChannels() {
        Runnable fetchTask = () -> {
            try {
                List<ChatRoom> rooms = service.getUserChatRooms(user.getUsername());
                if (rooms != null) {
                    SwingUtilities.invokeLater(() -> view.getChatList().setUserChannels(rooms));
                }
            } catch (RemoteException e) {
                System.err.println("Błąd odświeżania kanałów: " + e.getMessage());
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            new Thread(fetchTask).start();
        } else {
            fetchTask.run();
        }
    }

    private void setCurrentChatroom(ChatRoom newChatroom) {
        if (newChatroom == null) return;
        currentChatroom = newChatroom;
        new Thread(() -> {
            try {
                List<Message> messages = service.getChatHistory(newChatroom.getId());
                SwingUtilities.invokeLater(() -> {
                    view.clearMessages();
                    if (messages != null) {
                        for (Message msg : messages) {
                            view.appendMessage(String.format("%s: %s", msg.getSenderUsername(), msg.getContent()));
                        }
                    }
                });
            } catch (RemoteException e) {
                System.out.println("Błąd pobierania historii: " + e.getMessage());
            }
        }).start();
    }

    private class SendButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (currentChatroom == null) {
                return;
            }

            String text = view.getInputPanel().getMessageText();
            if (text == null || text.trim().isEmpty()) return;

            new Thread(() -> {
                try {
                    service.sendMessage(new Message(currentChatroom.getId(), text, user.getUsername()));
                    SwingUtilities.invokeLater(() -> view.getInputPanel().clearInput());
                } catch (RemoteException ex) {
                    System.out.println("Błąd wysyłania wiadomości: " + ex.getMessage());
                }
            }).start();
        }
    }

    private class NewChatroomButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(() -> {
                try {
                    List<User> registeredUsers = service.getRegisteredUsers();
                    List<String> userNames = registeredUsers.stream()
                            .map(User::getUsername)
                            .filter(name -> !name.equals(user.getUsername()))
                            .collect(Collectors.toList());

                    SwingUtilities.invokeLater(() -> {
                        ChatFrame.ChannelCreationResult result = ChatFrame.showNewChannelModal(view, userNames);
                        if (result != null) {
                            new Thread(() -> {
                                try {
                                    List<String> participants = new ArrayList<>(result.participants);
                                    if (!participants.contains(user.getUsername())) {
                                        participants.add(user.getUsername());
                                    }
                                    service.createChatRoom(result.name, participants);
                                } catch (RemoteException ex) {
                                    System.out.println("Błąd tworzenia pokoju: " + ex.getMessage());
                                }
                            }).start();
                        }
                    });
                } catch (RemoteException ex) {
                    System.out.println("Błąd pobierania użytkowników: " + ex.getMessage());
                }
            }).start();
        }
    }

    private class NewDMButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(() -> {
                try {
                    List<User> registeredUsers = service.getRegisteredUsers();
                    List<String> userNames = registeredUsers.stream()
                            .map(User::getUsername)
                            .filter(name -> !name.equals(user.getUsername()))
                            .collect(Collectors.toList());

                    SwingUtilities.invokeLater(() -> {
                        if (userNames.isEmpty()) {
                            JOptionPane.showMessageDialog(view, "Brak innych użytkowników do rozmowy.");
                            return;
                        }

                        String selectedUser = ChatFrame.showNewDMModal(view, userNames);
                        if (selectedUser != null) {
                            new Thread(() -> {
                                try {
                                    service.getOrCreateDirectMessage(user.getUsername(), selectedUser);
                                    refreshUserChannels();
                                } catch (RemoteException ex) {
                                    System.out.println("Błąd tworzenia DM: " + ex.getMessage());
                                }
                            }).start();
                        }
                    });
                } catch (RemoteException ex) {
                    System.out.println("Błąd pobierania użytkowników: " + ex.getMessage());
                }
            }).start();
        }
    }

    private class ChatListSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                setCurrentChatroom(view.getChatList().getSelectedChatroom());
            }
        }
    }

    @Override
    public void receiveMessage(Message message) throws RemoteException {
        if (currentChatroom != null && message != null && message.getChatRoomId().equals(currentChatroom.getId())) {
            SwingUtilities.invokeLater(() -> 
                view.appendMessage(String.format("%s: %s", message.getSenderUsername(), message.getContent()))
            );
        }
    }

    @Override
    public void updateOnlineUsers(List<User> users) throws RemoteException {
    }

    @Override
    public void notifyAddedToChatRoom(ChatRoom chatRoom) throws RemoteException {
        refreshUserChannels();
    }
}
