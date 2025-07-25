import javax.swing.*;

public class LibrarianGUI extends JFrame {
    public LibrarianGUI(User user) {
        setTitle("Librarian Dashboard");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel label = new JLabel("Welcome Librarian: " + user.username(), SwingConstants.CENTER);
        add(label);
        setVisible(true);
    }
}

