import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class LibrarianDashboard extends JFrame {
    private final User user;
    private final BookStorage bookStorage;
    private JTable bookTable;
    private DefaultTableModel model;

    public LibrarianDashboard(User user) {
        this.user = user;
        // Nếu bạn có user.getStorage(), giữ nguyên. Nếu không, đổi sang user.getLib().getStorage()
        this.bookStorage = user.getStorage();

        setTitle("Librarian Dashboard - " + user.username());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Title
        JLabel title = new JLabel("Books in Library", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        // Table (có cột Quantity)
        model = new DefaultTableModel(new Object[]{"Select", "ID", "Title", "Author", "Quantity"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : Object.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // chỉ cho tick checkbox
            }
        };
        bookTable = new JTable(model);
        bookTable.setRowHeight(24);
        add(new JScrollPane(bookTable), BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh");
        JButton addButton = new JButton("Add Book");
        JButton removeButton = new JButton("Remove Selected");
        JButton editButton = new JButton("Edit Selected");
        JButton logoutButton = new JButton("Logout");

        refreshButton.addActionListener(e -> refreshBookTable());
        addButton.addActionListener(e -> showAddBookDialog());
        removeButton.addActionListener(e -> removeSelectedBooks());
        editButton.addActionListener(e -> editSelectedBook());
        logoutButton.addActionListener(e -> doLogout());

        buttonPanel.add(refreshButton);
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(editButton);
        buttonPanel.add(logoutButton);
        add(buttonPanel, BorderLayout.SOUTH);

        refreshBookTable();
        setVisible(true);
    }

    // Load lại dữ liệu từ file và đổ ra bảng
    private void refreshBookTable() {
        model.setRowCount(0);
        bookStorage.reloadBooksFromFile();
        for (Book b : bookStorage.getBooks()) {
            // Nếu Book đã có getQuantity(), dùng trực tiếp; nếu chưa có, fallback = 1
            int qty = 1;
            try {
                qty = (int) Book.class.getMethod("getQuantity").invoke(b);
            } catch (Exception ignore) {}
            model.addRow(new Object[]{false, b.getId(), b.getTitle(), b.getAuthor(), qty});
        }
    }

    // Form thêm sách: có Quantity
    private void showAddBookDialog() {
        JDialog dialog = new JDialog(this, "Add New Book", true);
        dialog.setSize(380, 260);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.setLocationRelativeTo(this);

        JTextField idField = new JTextField();
        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField quantityField = new JTextField("1");

        dialog.add(new JLabel("ID:"));
        dialog.add(idField);
        dialog.add(new JLabel("Title:"));
        dialog.add(titleField);
        dialog.add(new JLabel("Author:"));
        dialog.add(authorField);
        dialog.add(new JLabel("Quantity:"));
        dialog.add(quantityField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String title = titleField.getText().trim();
                String author = authorField.getText().trim();
                int quantity = Integer.parseInt(quantityField.getText().trim());

                if (title.isEmpty() || author.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "All fields are required.");
                    return;
                }
                if (quantity < 1) {
                    JOptionPane.showMessageDialog(dialog, "Quantity must be >= 1.");
                    return;
                }

                Book existing = bookStorage.findBookById(id);
                if (existing == null) {
                    // ID chưa tồn tại -> thêm quantity bản
                    for (int i = 0; i < quantity; i++) {
                        bookStorage.addBook(new Book(id, title, author));
                    }
                } else {
                    // ID đã tồn tại -> kiểm tra Title & Author
                    boolean sameTitle = safeEqualsIgnoreCase(existing.getTitle(), title);
                    boolean sameAuthor = safeEqualsIgnoreCase(existing.getAuthor(), author);
                    if (sameTitle && sameAuthor) {
                        // tăng quantity bằng cách gọi addBook nhiều lần
                        for (int i = 0; i < quantity; i++) {
                            bookStorage.addBook(new Book(id, title, author));
                        }
                    } else {
                        JOptionPane.showMessageDialog(dialog,
                                "ID already exists with a different Title/Author.\nAction blocked.");
                        return;
                    }
                }

                refreshBookTable();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "ID and Quantity must be integers.");
            }
        });

        dialog.add(new JLabel());
        dialog.add(saveButton);
        dialog.setVisible(true);
    }

    // Remove: mở lựa chọn remove 1 / remove ALL / remove N
    private void removeSelectedBooks() {
        java.util.List<Integer> selectedIds = new java.util.ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (Boolean.TRUE.equals(model.getValueAt(i, 0))) {
                selectedIds.add(((Number) model.getValueAt(i, 1)).intValue());
            }
        }
        if (selectedIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No books selected.");
            return;
        }

        String[] options = { "Remove 1 copy / ID", "Remove ALL copies / ID", "Remove N copies / ID..." };
        int choice = JOptionPane.showOptionDialog(
                this, "Choose remove mode:", "Remove Options",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]
        );

        if (choice == 0) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Remove one copy for each selected book?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            for (int id : selectedIds) bookStorage.removeBookById(id);
            refreshBookTable();

        } else if (choice == 1) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Remove ALL copies for each selected book?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            for (int id : selectedIds) bookStorage.removeAllCopiesById(id);
            refreshBookTable();

        } else if (choice == 2) {
            String input = JOptionPane.showInputDialog(this, "Enter N (copies to remove per ID):", "1");
            if (input == null) return; // cancel
            try {
                int n = Integer.parseInt(input.trim());
                if (n <= 0) {
                    JOptionPane.showMessageDialog(this, "N must be >= 1.");
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Remove " + n + " copies for each selected book?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) return;

                for (int id : selectedIds) bookStorage.removeCopiesById(id, n);
                refreshBookTable();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "N must be an integer.");
            }
        }
    }

    // Edit một dòng (đã sửa cú pháp getValueAt)
    private void editSelectedBook() {
        int selectedRow = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (Boolean.TRUE.equals(model.getValueAt(i, 0))) {
                if (selectedRow != -1) {
                    JOptionPane.showMessageDialog(this, "Please select only one book to edit.");
                    return;
                }
                selectedRow = i;
            }
        }
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to edit.");
            return;
        }

        int oldId     = ((Number) model.getValueAt(selectedRow, 1)).intValue();
        String oldTitle  = String.valueOf(model.getValueAt(selectedRow, 2));
        String oldAuthor = String.valueOf(model.getValueAt(selectedRow, 3));
        int oldQty    = ((Number) model.getValueAt(selectedRow, 4)).intValue();

        JDialog dialog = new JDialog(this, "Edit Book", true);
        dialog.setSize(380, 240);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));
        dialog.setLocationRelativeTo(this);

        JTextField idField = new JTextField(String.valueOf(oldId));
        JTextField titleField = new JTextField(oldTitle);
        JTextField authorField = new JTextField(oldAuthor);

        dialog.add(new JLabel("New ID:"));
        dialog.add(idField);
        dialog.add(new JLabel("Title:"));
        dialog.add(titleField);
        dialog.add(new JLabel("Author:"));
        dialog.add(authorField);

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> {
            try {
                int newId = Integer.parseInt(idField.getText().trim());
                String newTitle = titleField.getText().trim();
                String newAuthor = authorField.getText().trim();

                if (newTitle.isEmpty() || newAuthor.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "All fields are required.");
                    return;
                }

                Book src = bookStorage.findBookById(oldId);
                if (src == null) {
                    JOptionPane.showMessageDialog(dialog, "Original book not found.");
                    return;
                }

                if (newId == oldId) {
                    // Không đổi ID -> đổi Title/Author, giữ nguyên tổng quantity
                    for (int i = 0; i < oldQty; i++) bookStorage.removeBookById(oldId);
                    for (int i = 0; i < oldQty; i++) bookStorage.addBook(new Book(newId, newTitle, newAuthor));
                } else {
                    // Đổi ID
                    Book dst = bookStorage.findBookById(newId);
                    if (dst == null) {
                        // ID mới chưa tồn tại -> di chuyển toàn bộ số lượng sang ID mới
                        for (int i = 0; i < oldQty; i++) bookStorage.removeBookById(oldId);
                        for (int i = 0; i < oldQty; i++) bookStorage.addBook(new Book(newId, newTitle, newAuthor));
                    } else {
                        // ID đích đã tồn tại -> phải trùng Title/Author để gộp
                        boolean sameTitle = safeEqualsIgnoreCase(dst.getTitle(), newTitle);
                        boolean sameAuthor = safeEqualsIgnoreCase(dst.getAuthor(), newAuthor);
                        if (!sameTitle || !sameAuthor) {
                            JOptionPane.showMessageDialog(dialog,
                                    "Target ID exists with a different Title/Author.\nAction blocked.");
                            return;
                        }
                        // Gộp: xoá toàn bộ oldId và thêm oldQty bản vào newId
                        for (int i = 0; i < oldQty; i++) bookStorage.removeBookById(oldId);
                        for (int i = 0; i < oldQty; i++) bookStorage.addBook(new Book(newId, newTitle, newAuthor));
                    }
                }

                refreshBookTable();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "ID must be an integer.");
            }
        });

        dialog.add(new JLabel());
        dialog.add(updateButton);
        dialog.setVisible(true);
    }

    // Đăng xuất → quay lại LoginFrame (đúng constructor)
    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this, "Do you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        dispose();

        SwingUtilities.invokeLater(() -> {
            // Dùng đúng constructor có tham số UserDatabase
            new LoginFrame(new UserDatabase());
        });
    }

    // helper
    private boolean safeEqualsIgnoreCase(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equalsIgnoreCase(b);
    }
}
