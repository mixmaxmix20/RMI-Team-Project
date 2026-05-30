import javax.swing.*;

public class Client {
    static void main() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ChatFrame mainFrame = new ChatFrame();
                mainFrame.setVisible(true);
            }
        });
    }
}
