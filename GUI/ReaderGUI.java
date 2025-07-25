import javax.swing.*;

public class ReaderGUI extends JFrame {
    public ReaderGUI(User user) {
        setTitle("Reader Dashboard");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel label = new JLabel("Welcome Reader: " + user.username(), SwingConstants.CENTER);
        add(label);
        setVisible(true);
    }
}
