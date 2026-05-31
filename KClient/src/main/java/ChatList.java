import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ChatList extends JPanel {
    private JList<ChatRoom> chatList;
    private DefaultListModel<ChatRoom> listModel;

    private JButton newChatroomButton;
    private JButton newDMBUtton;

    public ChatList() {
        super(new BorderLayout(0, 2));

        listModel = new DefaultListModel<>();

        chatList = new JList(listModel);
        chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatList.setCellRenderer(new ChatListRenderer());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        newChatroomButton = new JButton("Nowy kanał");
        newChatroomButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(newChatroomButton);

        newDMBUtton = new JButton("Nowa wiadomość prywatna");
        newDMBUtton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(newDMBUtton);

        add(new JScrollPane(chatList), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public ChatRoom getSelectedChatroom() {
        return chatList.getSelectedValue();
    }

    public void addListSelectionListener(ListSelectionListener l) {
        chatList.addListSelectionListener(l);
    }

    public void addNewChatroomButtonListener(ActionListener l) {
        newChatroomButton.addActionListener(l);
    }

    public void addNewDMButtonListener(ActionListener l) {
        newDMBUtton.addActionListener(l);
    }

    public void setUserChannels(List<ChatRoom> chatRooms) {
        listModel.clear();

        for (ChatRoom cr : chatRooms) {
            System.out.println(cr.getName());
            listModel.addElement(cr);
        }
    }

    public class ChatListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof ChatRoom) {
                ChatRoom cr = (ChatRoom)value;
                setText(cr.getName());
            }

            return this;
        }
    }
}
