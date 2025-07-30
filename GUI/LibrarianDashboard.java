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
    private JComboBox<String> scopeCombo; // Title / Author / ID / Title+Author / All
    private TableRowSorter<DefaultTableModel> sorter;

    // Column indexes (đỡ nhầm)
    private static final int COL_SELECT  = 0;
    private static final int COL_ID      = 1;
    private static final int COL_TITLE   = 2;
    private static final int COL_AUTHOR  = 3;
    private static final int COL_QTY     = 4;

    public LibrarianDashboard(User user) {
        this.user = user;
        this.bookStorage = user.getStorage();

        setTitle("Librarian Dashboard - " + user.username());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ===== TOP: Title + Search =====
        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Books in Library", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(title, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        searchField = new JTextField(24);
        scopeCombo = new JComboBox<>(new String[]{"Title", "Author", "ID", "Title+Author", "All"});
        scopeCombo.setSelectedItem("All"); // mặc định lọc cả 3 cột
        JButton clearButton = new JButton("Clear");
        searchPanel.add(new JLabel("Query:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("In:"));
        searchPanel.add(scopeCombo);
        searchPanel.add(clearButton);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // ===== CENTER: Table (có cột Quantity) =====
        model = new DefaultTableModel(new Object[]{"Select", "ID", "Title", "Author", "Quantity"}, 0) {
            @Override public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == COL_SELECT ? Boolean.class : Object.class;
            }
            @Override public boolean isCellEditable(int row, int column) {
                return column == COL_SELECT; // chỉ cho tick checkbox
            }
        };
        bookTable = new JTable(model);
        bookTable.setRowHeight(24);

        // Sorter + filter (live search)
        sorter = new TableRowSorter<>(model);
        sorter.setSortsOnUpdates(true);
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

        // ===== LIVE SEARCH =====
        // gõ tới đâu lọc tới đó
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void onChange() { applyFilter(searchField.getText().trim()); }
            public void insertUpdate(DocumentEvent e) { onChange(); }
            public void removeUpdate(DocumentEvent e) { onChange(); }
            public void changedUpdate(DocumentEvent e) { onChange(); }
        });
        // đổi phạm vi lọc → re-apply
        scopeCombo.addActionListener(e -> applyFilter(searchField.getText().trim()));
        // Clear
        clearButton.addActionListener(e -> {
            searchField.setText("");
            sorter.setRowFilter(null);
        });
        // Enter trong ô tìm kiếm = áp dụng filter (dù đã live)
        searchField.addActionListener(e -> applyFilter(searchField.getText().trim()));

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
            int qty = 1;
            try {
                qty = (int) Book.class.getMethod("getQuantity").invoke(b);
            } catch (Exception ignore) {}
            model.addRow(new Object[]{false, b.getId(), b.getTitle(), b.getAuthor(), qty});
        }

        // giữ nguyên filter hiện tại
        applyFilter(searchField.getText().trim());
    }

    // Áp dụng filter theo Title/Author/ID/Title+Author/All
    private void applyFilter(String query) {
        if (query == null || query.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        String pat = "(?i)" + Pattern.quote(query);
        String scope = (String) scopeCombo.getSelectedItem();

        if ("Title".equals(scope)) {
            sorter.setRowFilter(RowFilter.regexFilter(pat, COL_TITLE));
        } else if ("Author".equals(scope)) {
            sorter.setRowFilter(RowFilter.regexFilter(pat, COL_AUTHOR));
        } else if ("ID".equals(scope)) {
            // ID là Integer, regexFilter sẽ dùng toString() của giá trị -> vẫn match theo "contains"
            sorter.setRowFilter(RowFilter.regexFilter(Pattern.quote(query), COL_ID));
        } else if ("Title+Author".equals(scope)) {
            List<RowFilter<Object,Object>> ors = new ArrayList<>();
            ors.add(RowFilter.regexFilter(pat, COL_TITLE));
            ors.add(RowFilter.regexFilter(pat, COL_AUTHOR));
            sorter.setRowFilter(RowFilter.orFilter(ors));
        } else { // "All" = ID OR Title OR Author
            List<RowFilter<Object,Object>> ors = new ArrayList<>();
            ors.add(RowFilter.regexFilter(Pattern.quote(query), COL_ID)); // ID
            ors.add(RowFilter.regexFilter(pat, COL_TITLE));               // Title
            ors.add(RowFilter.regexFilter(pat, COL_AUTHOR));              // Author
            sorter.setRowFilter(RowFilter.orFilter(ors));
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
                    for (int i = 0; i < quantity; i++) {
                        bookStorage.addBook(new Book(id, title, author));
                    }
                } else {
                    boolean sameTitle = safeEqualsIgnoreCase(existing.getTitle(), title);
                    boolean sameAuthor = safeEqualsIgnoreCase(existing.getAuthor(), author);
                    if (sameTitle && sameAuthor) {
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

        dialog.add(new JLabel());  // spacing
        dialog.add(saveButton);
        dialog.setVisible(true);
    }

    // Remove: mở lựa chọn remove 1 / remove ALL / remove N
    private void removeSelectedBooks() {
        List<Integer> selectedIds = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (Boolean.TRUE.equals(model.getValueAt(i, COL_SELECT))) {
                selectedIds.add(((Number) model.getValueAt(i, COL_ID)).intValue());
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
            if (Boolean.TRUE.equals(model.getValueAt(i, COL_SELECT))) {
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

        int oldId     = ((Number) model.getValueAt(selectedModelRow, COL_ID)).intValue();
        String oldTitle  = String.valueOf(model.getValueAt(selectedModelRow, COL_TITLE));
        String oldAuthor = String.valueOf(model.getValueAt(selectedModelRow, COL_AUTHOR));
        int oldQty    = ((Number) model.getValueAt(selectedModelRow, COL_QTY)).intValue();

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
                    for (int i = 0; i < oldQty; i++) bookStorage.removeBookById(oldId);
                    for (int i = 0; i < oldQty; i++) bookStorage.addBook(new Book(newId, newTitle, newAuthor));
                } else {
                    Book dst = bookStorage.findBookById(newId);
                    if (dst == null) {
                        for (int i = 0; i < oldQty; i++) bookStorage.removeBookById(oldId);
                        for (int i = 0; i < oldQty; i++) bookStorage.addBook(new Book(newId, newTitle, newAuthor));
                    } else {
                        boolean sameTitle = safeEqualsIgnoreCase(dst.getTitle(), newTitle);
                        boolean sameAuthor = safeEqualsIgnoreCase(dst.getAuthor(), newAuthor);
                        if (!sameTitle || !sameAuthor) {
                            JOptionPane.showMessageDialog(dialog,
                                    "Target ID exists with a different Title/Author.\nAction blocked.");
                            return;
                        }
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

    // Đăng xuất → quay lại LoginFrame
    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this, "Do you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame(new UserDatabase()));
    }

    // Sau khi thêm/xoá/sửa: refresh bảng và giữ filter hiện tại
    private void afterMutateRefresh() {
        refreshBookTable();
    }

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

        im.put(KeyStroke.getKeyStroke("ENTER"), "editSelected");
        am.put("editSelected", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                editSelectedBook();
            }
        });
    }
}
