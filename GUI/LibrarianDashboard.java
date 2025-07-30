import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class LibrarianDashboard extends JFrame {
    private final User user;
    private final BookStorage bookStorage;
    private JTable bookTable;
    private DefaultTableModel model;

    // Search + sorter
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;

    public LibrarianDashboard(User user) {
        this.user = user;
        // Nếu bạn có user.getStorage(), giữ nguyên. Nếu không, đổi sang user.getLib().getStorage()
        this.bookStorage = user.getStorage();

        setTitle("Librarian Dashboard - " + user.username());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ===== TOP: Title + Search =====
        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Books in Library", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(title, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        searchField = new JTextField(24);
        JButton searchButton = new JButton("Search Title");
        JButton clearButton = new JButton("Clear");
        searchPanel.add(new JLabel("Title:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // ===== CENTER: Table (có cột Quantity) =====
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

        // Sorter + filter (live search)
        sorter = new TableRowSorter<>(model);
        bookTable.setRowSorter(sorter);

        add(new JScrollPane(bookTable), BorderLayout.CENTER);

        // ===== SOUTH: Buttons =====
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

        // ===== Search actions =====
        // Nút Search (vẫn giữ để không thay đổi thói quen), thực ra filter chạy live bên dưới
        searchButton.addActionListener(e -> applyFilter(searchField.getText().trim()));
        clearButton.addActionListener(e -> {
            searchField.setText("");
            applyFilter(null);
        });
        // Enter trong ô tìm kiếm = Search
        searchField.addActionListener(e -> applyFilter(searchField.getText().trim()));
        // LIVE SEARCH: gõ đến đâu lọc đến đó
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void onChange() { applyFilter(searchField.getText().trim()); }
            public void insertUpdate(DocumentEvent e) { onChange(); }
            public void removeUpdate(DocumentEvent e) { onChange(); }
            public void changedUpdate(DocumentEvent e) { onChange(); }
        });

        // ===== UX: phím tắt + double‑click để edit =====
        setupShortcuts();
        bookTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && bookTable.getSelectedRow() != -1) {
                    editSelectedBook();
                }
            }
        });

        refreshBookTable();
        setVisible(true);
    }

    // Load lại dữ liệu từ file và đổ ra bảng (tất cả)
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

        // Sau khi reload dữ liệu, giữ nguyên filter đang có
        applyFilter(searchField.getText().trim());
    }

    // Áp dụng filter cho sorter (lọc theo Title: cột 2; có thể mở rộng OR với Author: cột 3)
    private void applyFilter(String query) {
        if (query == null || query.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        String q = Pattern.quote(query);
        // Lọc theo Title (cột 2), không phân biệt hoa thường
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + q, 2));
        // Nếu muốn cả Author: 
        // sorter.setRowFilter(RowFilter.orFilter(List.of(
        //     RowFilter.regexFilter("(?i)" + q, 2),
        //     RowFilter.regexFilter("(?i)" + q, 3)
        // )));
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
        List<Integer> selectedIds = new ArrayList<>();
        // Duyệt MODEL để lấy các dòng đã tick (checkbox nằm trong model nên an toàn với filter/sort)
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
            afterMutateRefresh();

        } else if (choice == 1) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Remove ALL copies for each selected book?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            for (int id : selectedIds) bookStorage.removeAllCopiesById(id);
            afterMutateRefresh();

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
                afterMutateRefresh();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "N must be an integer.");
            }
        }
    }

    // Edit một dòng (dựa trên checkbox tick đúng 1 dòng)
    private void editSelectedBook() {
        int selectedModelRow = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (Boolean.TRUE.equals(model.getValueAt(i, 0))) {
                if (selectedModelRow != -1) {
                    JOptionPane.showMessageDialog(this, "Please select only one book to edit.");
                    return;
                }
                selectedModelRow = i;
            }
        }
        if (selectedModelRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to edit.");
            return;
        }

        int oldId     = ((Number) model.getValueAt(selectedModelRow, 1)).intValue();
        String oldTitle  = String.valueOf(model.getValueAt(selectedModelRow, 2));
        String oldAuthor = String.valueOf(model.getValueAt(selectedModelRow, 3));
        int oldQty    = ((Number) model.getValueAt(selectedModelRow, 4)).intValue();

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

                afterMutateRefresh();
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

    // Sau khi thêm/xoá/sửa: refresh bảng và giữ filter hiện tại
    private void afterMutateRefresh() {
        refreshBookTable();
    }

    // helper
    private boolean safeEqualsIgnoreCase(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equalsIgnoreCase(b);
    }

    // ===== phím tắt tiện dụng =====
    private void setupShortcuts() {
        InputMap im = bookTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = bookTable.getActionMap();

        im.put(KeyStroke.getKeyStroke("control F"), "focusSearch");
        am.put("focusSearch", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                searchField.requestFocusInWindow();
                searchField.selectAll();
            }
        });

        im.put(KeyStroke.getKeyStroke("control N"), "addBook");
        am.put("addBook", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                showAddBookDialog();
            }
        });

        im.put(KeyStroke.getKeyStroke("DELETE"), "removeSelected");
        am.put("removeSelected", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                removeSelectedBooks();
            }
        });
    }
}
