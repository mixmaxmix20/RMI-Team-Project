import javax.swing.*;
import java.awt.*;
import java.util.List;

public class UserList extends JPanel {
    private JList<User> userList;
    private DefaultListModel<User> listModel;

    public UserList() {
        super(new BorderLayout());
        setPreferredSize(new Dimension(150, 0));
        setBorder(BorderFactory.createTitledBorder("Użytkownicy"));

        listModel = new DefaultListModel<>();
        userList = new JList<>(listModel);
        userList.setCellRenderer(new UserListRenderer());

        add(new JScrollPane(userList), BorderLayout.CENTER);
    }

    public void setUsers(List<User> users) {
        listModel.clear();
        if (users != null) {
            for (User user : users) {
                listModel.addElement(user);
            }
        }
    }

    private static class UserListRenderer extends DefaultListCellRenderer {
        private static final int ICON_SIZE = 10;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof User) {
                User user = (User) value;
                label.setText(user.getUsername());
                label.setIcon(new OnlineIcon(user.isOnline()));
            }
            
            return label;
        }

        private static class OnlineIcon implements Icon {
            private final boolean online;

            public OnlineIcon(boolean online) {
                this.online = online;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(online ? Color.GREEN : Color.GRAY);
                g2d.fillOval(x, y + (getIconHeight() - ICON_SIZE) / 2, ICON_SIZE, ICON_SIZE);
                g2d.dispose();
            }

            @Override
            public int getIconWidth() {
                return ICON_SIZE + 5;
            }

            @Override
            public int getIconHeight() {
                return ICON_SIZE;
            }
        }
    }
}
