import javax.swing.*;
import java.awt.*;

public class ChatFrame extends JFrame {
    private JTextPane textPane;
    private InputPanel inputPanel;

    public ChatFrame() {
        setTitle("Komunikator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(0, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFocusable(false);
        mainPanel.add(new JScrollPane(textPane), BorderLayout.CENTER);

        inputPanel = new InputPanel();
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    public InputPanel getInputPanel() {
        return inputPanel;
    }

    public void appendMessage(String message) {
        textPane.setText(textPane.getText() + message + "\n");
    }
}
