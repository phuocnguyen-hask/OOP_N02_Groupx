import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private UserDatabase userDatabase;

    public LoginFrame(UserDatabase userDatabase) {
        this.userDatabase = userDatabase;

        setTitle("Library Login");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // center

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        JButton loginButton = new JButton("Login");
        panel.add(loginButton);

        JButton exitButton = new JButton("Exit");
        panel.add(exitButton);

        add(panel);
        setVisible(true);

        // Events
        loginButton.addActionListener(e -> attemptLogin());
        exitButton.addActionListener(e -> System.exit(0));
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        User user = userDatabase.findByUsername(username);

        if (user != null && user.passWord().equals(password)) {
            JOptionPane.showMessageDialog(this, "Login successful!", "Welcome", JOptionPane.INFORMATION_MESSAGE);

            // Proceed to next GUI or dashboard
            if (user.getReader() instanceof ReaderUser) {
                new ReaderDashboard(user);  // You can define this class later
            } else if (user.getLib() instanceof LibrarianUser) {
                new LibrarianDashboard(user);  // You can define this class too
            }

            dispose();  // Close login window
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
