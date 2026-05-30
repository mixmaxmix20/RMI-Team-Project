import javax.swing.*;
import java.awt.*;
import java.util.jar.JarEntry;

public class Client {
    static void main() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setupGUI();
            }
        });
    }

    private static void setupGUI() {
        JFrame frame = new JFrame("Komunikator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFocusable(false);
        panel.add(textPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        JTextField textField = new JTextField();
        bottomPanel.add(textField, BorderLayout.CENTER);

        JButton sendButton = new JButton("Wyślij");
        bottomPanel.add(sendButton, BorderLayout.EAST);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        frame.add(panel);

        frame.setVisible(true);
    }
}
