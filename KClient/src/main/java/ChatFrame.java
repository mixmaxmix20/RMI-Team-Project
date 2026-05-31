import javax.swing.*;
import java.awt.*;
import java.util.Collections;

public class ChatFrame extends JFrame {
    private JTextPane textPane;
    private InputPanel inputPanel;
    private ChatList chatList;

    public ChatFrame() {
        setTitle("Komunikator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFocusable(false);
        mainPanel.add(new JScrollPane(textPane), BorderLayout.CENTER);

        chatList = new ChatList();
        mainPanel.add(chatList, BorderLayout.WEST);

        inputPanel = new InputPanel();
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    public InputPanel getInputPanel() {
        return inputPanel;
    }

    public ChatList getChatList() {
        return chatList;
    }

    public void appendMessage(String message) {
        textPane.setText(textPane.getText() + message + "\n");
    }

    public void clearMessages() {
        textPane.setText("");
    }

    public static String showUsernameModal(Component parent) {
        JTextField textField = new JTextField(15);
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        panel.add(new JLabel("Podaj nazwę użytkownika:"));
        panel.add(textField);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Nazwa użytkownika",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        return (result == JOptionPane.OK_OPTION) ? textField.getText() : null;
    }

    public static class ChannelCreationResult {
        public final String name;
        public final java.util.List<String> participants;

        public ChannelCreationResult(String name, java.util.List<String> participants) {
            this.name = name;
            this.participants = participants;
        }
    }

    public static ChannelCreationResult showNewChannelModal(Component parent, java.util.List<String> options) {
        JTextField nameField = new JTextField(15);
        JList<String> list = new JList<>(options.toArray(new String[0]));
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setVisibleRowCount(8);

        JScrollPane scrollPane = new JScrollPane(list);

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JPanel namePanel = new JPanel(new BorderLayout(5, 5));
        namePanel.add(new JLabel("Nazwa kanału:"), BorderLayout.NORTH);
        namePanel.add(nameField, BorderLayout.CENTER);

        panel.add(namePanel, BorderLayout.NORTH);
        JPanel participantsPanel = new JPanel(new BorderLayout(5, 5));
        participantsPanel.add(new JLabel("Wybierz użytkowników:"), BorderLayout.NORTH);
        participantsPanel.add(scrollPane, BorderLayout.CENTER);
        panel.add(participantsPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(
                parent,
                panel,
                "Stwórz kanał",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION && !nameField.getText().trim().isEmpty()) {
            return new ChannelCreationResult(nameField.getText().trim(), list.getSelectedValuesList());
        }

        return null;
    }

    public static String showNewDMModal(Component parent, java.util.List<String> options) {
        JComboBox<String> comboBox = new JComboBox<>(options.toArray(new String[0]));
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        panel.add(new JLabel("Wybierz użytkownika do rozmowy:"));
        panel.add(comboBox);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Nowa wiadomość prywatna",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        return (result == JOptionPane.OK_OPTION) ? (String) comboBox.getSelectedItem() : null;
    }
}
