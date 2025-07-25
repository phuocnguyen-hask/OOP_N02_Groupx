import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    private ArrayList<User> users;

    public LoginFrame(ArrayList<User> users) {
        this.users = users;
        setTitle("Library Login");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        setVisible(true);
    }

    private void initUI() {
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel(""));
        panel.add(loginButton);

        add(panel);

        loginButton.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        for (User u : users) {
            if (u.username().equals(username) && u.passWord().equals(password)) {
                // TEMP: let's simulate role setup
                if (username.contains("lib")) {
                    u.setLibrarianRole();
                } else {
                    u.setReaderRole();
                }

                if (u instanceof User) {
                    if (u.getLib() instanceof LibrarianUser) {
                        JOptionPane.showMessageDialog(this, "Logged in as Librarian!");
                        new LibrarianGUI(u);
                    } else if (u.getReader() instanceof ReaderUser) {
                        JOptionPane.showMessageDialog(this, "Logged in as Reader!");
                        new ReaderGUI(u);
                    } else {
                        JOptionPane.showMessageDialog(this, "Role not assigned.");
                    }
                }
                dispose();
                return;
            }
        }

        JOptionPane.showMessageDialog(this, "Invalid credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
    }
}
