import javax.swing.*;
import java.awt.*;

public class LibrarianDashboard extends JFrame {
    private User user;

    public LibrarianDashboard(User user) {
        this.user = user;
        setTitle("Librarian Dashboard - " + user.username());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);

        JLabel welcomeLabel = new JLabel("Welcome, Librarian: " + user.username(), SwingConstants.CENTER);
        add(welcomeLabel, BorderLayout.CENTER);

        // You can add more librarian functionalities here

        setVisible(true);
    }
}
