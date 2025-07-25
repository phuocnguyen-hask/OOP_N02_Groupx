import javax.swing.*;
import java.awt.*;

public class ReaderDashboard extends JFrame {
    private User user;

    public ReaderDashboard(User user) {
        this.user = user;
        setTitle("Reader Dashboard - " + user.username());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);

        JLabel welcomeLabel = new JLabel("Welcome, Reader: " + user.username(), SwingConstants.CENTER);
        add(welcomeLabel, BorderLayout.CENTER);

        // You can add more reader functionalities here

        setVisible(true);
    }
}
