import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox showPwd;
    private UserDatabase userDatabase;
    private BorrowStorage borrowStorage; // dùng để truyền vào User khi đăng ký

    /* ===================== Constructors ===================== */

    // Cách 1: không tham số -> tự tạo DB + BorrowStorage (đọc file sẵn có)
    public LoginFrame() {
        this(new UserDatabase(), createDefaultBorrowStorage());
    }

    // Cách 2: có UserDatabase -> tự tạo BorrowStorage
    public LoginFrame(UserDatabase userDatabase) {
        this(userDatabase, createDefaultBorrowStorage());
    }

    // Cách 3 (khuyến nghị): truyền vào DB + BorrowStorage dùng chung toàn app
    public LoginFrame(UserDatabase userDatabase, BorrowStorage borrowStorage) {
        this.userDatabase = userDatabase;
        this.borrowStorage = borrowStorage;

        setTitle("Library Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 320);
        setLocationRelativeTo(null);

        /* ===== Background (nhẹ CPU, không blur) ===== */
        Image bg = loadBackgroundImage(); // /images/lib.jpg hoặc ./images/lib.jpg
        BackgroundPanel bgPanel = new BackgroundPanel(bg);
        setContentPane(bgPanel); // dùng panel nền làm contentPane
        bgPanel.setLayout(new GridBagLayout());

        /* ===== Form (trong panel trong suốt) ===== */
        JPanel formWrapper = new TranslucentPanel(); // nền mờ cho dễ đọc
        formWrapper.setLayout(new GridBagLayout());
        formWrapper.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        GridBagConstraints f = new GridBagConstraints();
        f.insets = new Insets(6, 6, 6, 6);
        f.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLbl = new JLabel("Username:");
        userLbl.setForeground(new Color(20, 20, 20));
        f.gridx = 0; f.gridy = 0; formWrapper.add(userLbl, f);

        usernameField = new JTextField(18);
        f.gridx = 1; f.gridy = 0; formWrapper.add(usernameField, f);

        JLabel passLbl = new JLabel("Password:");
        passLbl.setForeground(new Color(20, 20, 20));
        f.gridx = 0; f.gridy = 1; formWrapper.add(passLbl, f);

        passwordField = new JPasswordField(18);
        f.gridx = 1; f.gridy = 1; formWrapper.add(passwordField, f);

        showPwd = new JCheckBox("Show password");
        showPwd.setOpaque(false);
        showPwd.addActionListener(e -> {
            passwordField.setEchoChar(showPwd.isSelected() ? (char)0 : '\u2022');
        });
        f.gridx = 1; f.gridy = 2; formWrapper.add(showPwd, f);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton signupButton = new JButton("Create Account");
        JButton loginButton  = new JButton("Login");
        JButton exitButton   = new JButton("Exit");
        buttons.add(signupButton);
        buttons.add(loginButton);
        buttons.add(exitButton);

        f.gridx = 0; f.gridy = 3; f.gridwidth = 2; f.fill = GridBagConstraints.NONE; f.anchor = GridBagConstraints.EAST;
        formWrapper.add(buttons, f);

        // Đặt formWrapper vào giữa nền
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        bgPanel.add(formWrapper, c);

        // Events
        loginButton.addActionListener(e -> attemptLogin());
        signupButton.addActionListener(e -> showSignUpDialog());
        exitButton.addActionListener(e -> System.exit(0));
        getRootPane().setDefaultButton(loginButton);

        setVisible(true);
    }

    /* ===================== Actions ===================== */

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.");
            return;
        }

        User user = userDatabase.findByUsername(username);

        if (user != null && user.passWord().equals(password)) {
            JOptionPane.showMessageDialog(this, "Login successful!", "Welcome", JOptionPane.INFORMATION_MESSAGE);

            // Điều hướng dựa vào vai trò đã set trong User
            // đảm bảo user có BorrowStorage (nếu user cũ load từ file)
            if (user.getBorrowStorage() == null) {
                user.setBorrowStorage(this.borrowStorage);
            }

            // đảm bảo có BorrowStorage nếu user cũ thiếu
            if (user.getBorrowStorage() == null) user.setBorrowStorage(this.borrowStorage);

            if (user.isLibrarian()){
                UserDatabase userDatabase = new UserDatabase();
                new LibrarianDashboard(user, userDatabase);
            } 
            else new ReaderDashboard(user);
            dispose();

        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSignUpDialog() {
        JDialog dialog = new JDialog(this, "Create Account", true);
        dialog.setSize(400, 350);  // tăng chiều cao để vừa phoneNumber
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField newUserField = new JTextField();
        JPasswordField newPassField = new JPasswordField();
        JPasswordField confirmPassField = new JPasswordField();
        JTextField phoneField = new JTextField();  // <--- THÊM input phoneNumber
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Reader", "Librarian"});

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; dialog.add(newUserField, gbc);

        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; dialog.add(newPassField, gbc);

        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; dialog.add(confirmPassField, gbc);

        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Phone Number:"), gbc);  // <--- THÊM label phoneNumber
        gbc.gridx = 1; gbc.gridy = row++; dialog.add(phoneField, gbc);                // <--- THÊM input phoneNumber

        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; dialog.add(roleCombo, gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton createBtn = new JButton("Create");
        JButton cancelBtn = new JButton("Cancel");
        buttons.add(createBtn);
        buttons.add(cancelBtn);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST;
        dialog.add(buttons, gbc);

        cancelBtn.addActionListener(e -> dialog.dispose());
        createBtn.addActionListener(e -> {
            String username = newUserField.getText().trim();
            String pass = new String(newPassField.getPassword());
            String confirm = new String(confirmPassField.getPassword());
            String phoneNumber = phoneField.getText().trim();    // <--- Lấy giá trị phoneNumber
            String role = (String) roleCombo.getSelectedItem();

            if (username.isEmpty() || pass.isEmpty() || confirm.isEmpty() || phoneNumber.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required.");
                return;
            }
            if (!pass.equals(confirm)) {
                JOptionPane.showMessageDialog(dialog, "Passwords do not match.");
                return;
            }
            if (userDatabase.findByUsername(username) != null) {
                JOptionPane.showMessageDialog(dialog, "Username already exists.");
                return;
            }

            int newId = nextUserId();
            User newUser = new User(newId, username, pass, phoneNumber, this.borrowStorage);  // <--- gọi constructor mới có phoneNumber

            if ("Librarian".equalsIgnoreCase(role)) {
                newUser.setLibrarianRole();
            } else {
                newUser.setReaderRole();   // giữ nguyên vì ReaderUser lấy phoneNumber từ User
            }

            userDatabase.addUser(newUser);
            JOptionPane.showMessageDialog(dialog, "Account created successfully!");
            dialog.dispose();
        });

        dialog.getRootPane().setDefaultButton(createBtn);
        dialog.setVisible(true);
    }


    private int nextUserId() {
        ArrayList<User> all = userDatabase.getAllUsers();
        int max = 0;
        if (all != null) {
            for (User u : all) {
                if (u.getId() > max) max = u.getId();
            }
        }
        return max + 1;
    }

    /* ===================== Helpers ===================== */

    private static BorrowStorage createDefaultBorrowStorage() {
        BookStorage bookStorage = new BookStorage();
        return new BorrowStorage(bookStorage);
    }

    /** Tải ảnh nền: ưu tiên classpath (/images/lib.jpg), fallback thư mục ./images hoặc ./background.jpg */
    private static Image loadBackgroundImage() {
        try {
            // 1) classpath
            java.net.URL url = LoginFrame.class.getResource("/images/lib.jpg");
            if (url != null) return new ImageIcon(url).getImage();

            // 2) ./images/lib.jpg
            File f1 = new File("images/lib.jpg");
            if (f1.exists()) return new ImageIcon(f1.getAbsolutePath()).getImage();

            // 3) ./background.jpg (tuỳ bạn)
            File f2 = new File("background.jpg");
            if (f2.exists()) return new ImageIcon(f2.getAbsolutePath()).getImage();
        } catch (Exception ignore) { }
        return null; // không có ảnh → nền trơn
    }

    /* ===== Panel nền vẽ ảnh scale full frame (nhẹ CPU, có cache) ===== */
    private static class BackgroundPanel extends JPanel {
        private final Image originalImage;
        private Image scaled;                 // ảnh đã scale sẵn cho kích thước hiện tại
        private Dimension lastSize = new Dimension(0, 0);

        BackgroundPanel(Image image) {
            this.originalImage = image;
            setOpaque(true);
        }

        @Override
        public void invalidate() {
            super.invalidate();
            scaled = null; // khi panel đổi size/layout, xoá cache để scale lại
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;

            if (originalImage == null) {
                // nền trơn khi không có ảnh
                g.setColor(new Color(235, 238, 241));
                g.fillRect(0, 0, w, h);
                return;
            }

            // Chỉ scale lại khi kích thước thay đổi
            if (scaled == null || lastSize.width != w || lastSize.height != h) {
                scaled = originalImage.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                lastSize.setSize(w, h);
            }

            g.drawImage(scaled, 0, 0, this);
        }
    }

    /* ===== Panel translucent (nền mờ) cho form ===== */
    private static class TranslucentPanel extends JPanel {
        TranslucentPanel() { setOpaque(false); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 180)); // trắng mờ
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
