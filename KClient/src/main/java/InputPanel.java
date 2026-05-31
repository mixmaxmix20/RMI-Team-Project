import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class InputPanel extends JPanel {
    private JTextField textField;
    private JButton sendButton;

    public InputPanel() {
        setLayout(new BorderLayout());

        textField = new JTextField();
        sendButton = new JButton("Wyślij");

        add(textField, BorderLayout.CENTER);
        add(sendButton, BorderLayout.EAST);
    }

    public String getMessageText() {
        return textField.getText();
    }

    public void clearInput() {
        textField.setText("");
    }

    public void addSendButtonListener(ActionListener listener) {
        sendButton.addActionListener(listener);
        textField.addActionListener(listener);
    }
}
