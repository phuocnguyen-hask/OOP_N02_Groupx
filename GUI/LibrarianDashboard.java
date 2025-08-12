import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.time.LocalDate;

public class LibrarianDashboard extends JFrame {
    private final User user;
    private final BookStorage bookStorage;
    private final BorrowStorage borrowStorage;

    private final UserDatabase userDatabase;  // --- THÊM ---

    private JTable bookTable;
    private DefaultTableModel model;

    private JTextField searchField;
    private JComboBox<String> scopeCombo;
    private TableRowSorter<DefaultTableModel> sorter;

    private final RequestStorage requestStorage = new RequestStorage();

    private static final int COL_SELECT  = 0;
    private static final int COL_ID      = 1;
    private static final int COL_TITLE   = 2;
    private static final int COL_AUTHOR  = 3;
    private static final int COL_QTY     = 4;

    // Constructor: thêm userDatabase vào tham số và gán
    public LibrarianDashboard(User user, UserDatabase userDatabase) {
        this.user = user;
        this.userDatabase = userDatabase;  // --- THÊM ---
        this.bookStorage = user.getStorage();
        this.borrowStorage = user.getBorrowStorage();

        setTitle("Librarian Dashboard - " + user.username());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Books in Library", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(title, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        searchField = new JTextField(24);
        scopeCombo = new JComboBox<>(new String[]{"Title", "Author", "ID", "Title+Author", "All"});
        scopeCombo.setSelectedItem("All");
        JButton clearButton = new JButton("Clear");
        searchPanel.add(new JLabel("Query:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("In:"));
        searchPanel.add(scopeCombo);
        searchPanel.add(clearButton);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"Select", "ID", "Title", "Author", "Quantity"}, 0) {
            @Override public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == COL_SELECT ? Boolean.class : Object.class;
            }
            @Override public boolean isCellEditable(int row, int column) {
                return column == COL_SELECT;
            }
        };
        bookTable = new JTable(model);
        bookTable.setRowHeight(24);

        sorter = new TableRowSorter<>(model);
        sorter.setSortsOnUpdates(true);
        bookTable.setRowSorter(sorter);

        add(new JScrollPane(bookTable), BorderLayout.CENTER);

        // --- THÊM nút View Readers ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh");
        JButton addButton = new JButton("Add Book");
        JButton removeButton = new JButton("Remove Selected");
        JButton editButton = new JButton("Edit Selected");
        JButton requestsButton = new JButton("Requests...");
        JButton viewReadersButton = new JButton("View Readers");   // --- THÊM ---
        JButton logoutButton = new JButton("Logout");

        refreshButton.addActionListener(e -> refreshBookTable());
        addButton.addActionListener(e -> showAddBookDialog());
        removeButton.addActionListener(e -> removeSelectedBooks());
        editButton.addActionListener(e -> editSelectedBook());
        requestsButton.addActionListener(e -> showRequestsDialog());
        viewReadersButton.addActionListener(e -> showReadersDialog()); // --- THÊM ---
        logoutButton.addActionListener(e -> doLogout());

        buttonPanel.add(refreshButton);
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(editButton);
        buttonPanel.add(requestsButton);
        buttonPanel.add(viewReadersButton);   // --- THÊM ---
        buttonPanel.add(logoutButton);
        add(buttonPanel, BorderLayout.SOUTH);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void onChange() { applyFilter(searchField.getText().trim()); }
            public void insertUpdate(DocumentEvent e) { onChange(); }
            public void removeUpdate(DocumentEvent e) { onChange(); }
            public void changedUpdate(DocumentEvent e) { onChange(); }
        });
        scopeCombo.addActionListener(e -> applyFilter(searchField.getText().trim()));
        clearButton.addActionListener(e -> { searchField.setText(""); sorter.setRowFilter(null); });
        searchField.addActionListener(e -> applyFilter(searchField.getText().trim()));

        setupShortcuts();
        bookTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && bookTable.getSelectedRow() != -1) editSelectedBook();
            }
        });

        refreshBookTable();
        setVisible(true);
    }

    // Thêm hàm showEditPhoneDialog vào LibrarianDashboard
    private void showEditPhoneDialog(User user) {
        if (user == null) {
            JOptionPane.showMessageDialog(this, "No user selected.");
            return;
        }

        String currentPhone = user.getPhoneNumber() != null ? user.getPhoneNumber() : "";

        String newPhone = JOptionPane.showInputDialog(
            this,
            "Edit phone number for " + user.username() + ":",
            currentPhone
        );

        if (newPhone != null) {
            newPhone = newPhone.trim();
            if (newPhone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Phone number cannot be empty.");
                return;
            }
            user.setPhoneNumber(newPhone);

            // Thêm dòng này để lưu lại dữ liệu userDatabase sau khi sửa
            userDatabase.save();

            JOptionPane.showMessageDialog(this, "Phone number updated successfully.");
        }
    }


// Sửa lại phần showReadersDialog để thêm nút "Edit Phone"
    private void showReadersDialog() {
        JDialog dlg = new JDialog(this, "Readers List", true);
        dlg.setSize(700, 400);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(8, 8));

        DefaultTableModel modelReaders = new DefaultTableModel(
            new Object[]{"ID", "Username"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable readersTable = new JTable(modelReaders);
        readersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        readersTable.setRowHeight(24);

        // Load readers vào bảng
        for (User u : userDatabase.getAllUsers()) {
            if (u.getRole() == User.Role.READER) {
                modelReaders.addRow(new Object[]{u.getId(), u.username()});
            }
        }

        dlg.add(new JScrollPane(readersTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton viewDetailsBtn = new JButton("View Details");
        JButton editPhoneBtn = new JButton("Edit Phone");  // Nút mới
        JButton closeBtn = new JButton("Close");

        bottomPanel.add(viewDetailsBtn);
        bottomPanel.add(editPhoneBtn);  // Thêm nút
        bottomPanel.add(closeBtn);
        dlg.add(bottomPanel, BorderLayout.SOUTH);

        viewDetailsBtn.addActionListener(e -> {
            int selectedRow = readersTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(dlg, "Please select a reader first.");
                return;
            }
            int modelRow = readersTable.convertRowIndexToModel(selectedRow);
            int readerId = (Integer) modelReaders.getValueAt(modelRow, 0);
            User selectedUser = userDatabase.findById(readerId);

            if (selectedUser == null) {
                JOptionPane.showMessageDialog(dlg, "Selected reader not found.");
                return;
            }

            showReaderDetailDialog(selectedUser);
        });

        editPhoneBtn.addActionListener(e -> {
            int selectedRow = readersTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(dlg, "Please select a reader first.");
                return;
            }
            int modelRow = readersTable.convertRowIndexToModel(selectedRow);
            int readerId = (Integer) modelReaders.getValueAt(modelRow, 0);
            User selectedUser = userDatabase.findById(readerId);

            if (selectedUser == null) {
                JOptionPane.showMessageDialog(dlg, "Selected reader not found.");
                return;
            }

            showEditPhoneDialog(selectedUser);
        });

        closeBtn.addActionListener(e -> dlg.dispose());

        dlg.setVisible(true);
    }


    // Hàm hiện dialog chi tiết Reader và sách đang mượn
   private void showReaderDetailDialog(User user) {
        if (user == null) return;

        JDialog dialog = new JDialog(this, "Reader Details - " + user.username(), true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(8, 8));

        JPanel infoPanel = new JPanel(new GridLayout(3, 2, 8, 8));  // sửa 2 -> 3 dòng để thêm số điện thoại
        infoPanel.add(new JLabel("Reader ID:"));
        infoPanel.add(new JLabel(String.valueOf(user.getId())));
        infoPanel.add(new JLabel("Username:"));
        infoPanel.add(new JLabel(user.username()));
        infoPanel.add(new JLabel("Phone Number:"));   // thêm label mới
        infoPanel.add(new JLabel(user.getPhoneNumber() != null ? user.getPhoneNumber() : ""));

        dialog.add(infoPanel, BorderLayout.NORTH);

        // Lấy sách mượn của reader
        List<BorrowBook> borrowedBooks = user.getBorrowStorage().listBorrowedByReader(user.getId());

        DefaultTableModel borrowModel = new DefaultTableModel(
            new Object[]{"Book ID", "Title", "Author", "Borrow Date", "Return Date"}, 0
        );
        for (BorrowBook b : borrowedBooks) {
            borrowModel.addRow(new Object[]{
                b.getBook().getId(),
                b.getBook().getTitle(),
                b.getBook().getAuthor(),
                b.getBorrowDate(),
                b.getReturnDate()
            });
        }
        JTable borrowTable = new JTable(borrowModel);
        borrowTable.setRowHeight(24);

        dialog.add(new JScrollPane(borrowTable), BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(closeBtn);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }




    private void refreshBookTable() {
        model.setRowCount(0);
        bookStorage.reloadBooksFromFile();

        for (Book b : bookStorage.getBooks()) {
            int qty = 1;
            try { qty = (int) Book.class.getMethod("getQuantity").invoke(b); } catch (Exception ignore) {}
            model.addRow(new Object[]{false, b.getId(), b.getTitle(), b.getAuthor(), qty});
        }
        applyFilter(searchField.getText().trim());
    }

    //books filter
    private void applyFilter(String query) {
        if (query == null || query.isEmpty()) { sorter.setRowFilter(null); return; }
        String pat = "(?i)" + Pattern.quote(query);
        String scope = (String) scopeCombo.getSelectedItem();

        if ("Title".equals(scope)) {
            sorter.setRowFilter(RowFilter.regexFilter(pat, COL_TITLE));
        } else if ("Author".equals(scope)) {
            sorter.setRowFilter(RowFilter.regexFilter(pat, COL_AUTHOR));
        } else if ("ID".equals(scope)) {
            sorter.setRowFilter(RowFilter.regexFilter(Pattern.quote(query), COL_ID));
        } else if ("Title+Author".equals(scope)) {
            List<RowFilter<Object,Object>> ors = new ArrayList<>();
            ors.add(RowFilter.regexFilter(pat, COL_TITLE));
            ors.add(RowFilter.regexFilter(pat, COL_AUTHOR));
            sorter.setRowFilter(RowFilter.orFilter(ors));
        } else {
            List<RowFilter<Object,Object>> ors = new ArrayList<>();
            ors.add(RowFilter.regexFilter(Pattern.quote(query), COL_ID));
            ors.add(RowFilter.regexFilter(pat, COL_TITLE));
            ors.add(RowFilter.regexFilter(pat, COL_AUTHOR));
            sorter.setRowFilter(RowFilter.orFilter(ors));
        }
    }

    private void showAddBookDialog() {
        JDialog dialog = new JDialog(this, "Add New Book", true);
        dialog.setSize(380, 260);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.setLocationRelativeTo(this);

        JTextField idField = new JTextField();
        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField quantityField = new JTextField("1");

        dialog.add(new JLabel("ID:"));      dialog.add(idField);
        dialog.add(new JLabel("Title:"));   dialog.add(titleField);
        dialog.add(new JLabel("Author:"));  dialog.add(authorField);
        dialog.add(new JLabel("Quantity:"));dialog.add(quantityField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String title = titleField.getText().trim();
                String author = authorField.getText().trim();
                int quantity = Integer.parseInt(quantityField.getText().trim());

                if (title.isEmpty() || author.isEmpty()) { JOptionPane.showMessageDialog(dialog, "All fields are required."); return; }
                if (quantity < 1) { JOptionPane.showMessageDialog(dialog, "Quantity must be >= 1."); return; }

                Book existing = bookStorage.findBookById(id);
                if (existing == null) {
                    for (int i = 0; i < quantity; i++) bookStorage.addBook(new Book(id, title, author));
                } else {
                    boolean sameTitle = safeEqualsIgnoreCase(existing.getTitle(), title);
                    boolean sameAuthor = safeEqualsIgnoreCase(existing.getAuthor(), author);
                    if (sameTitle && sameAuthor) {
                        for (int i = 0; i < quantity; i++) bookStorage.addBook(new Book(id, title, author));
                    } else {
                        JOptionPane.showMessageDialog(dialog, "ID already exists with a different Title/Author.\nAction blocked.");
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

    //remove books
    private void removeSelectedBooks() {
        List<Integer> selectedIds = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (Boolean.TRUE.equals(model.getValueAt(i, COL_SELECT))) {
                selectedIds.add(((Number) model.getValueAt(i, COL_ID)).intValue());
            }
        }
        if (selectedIds.isEmpty()) { JOptionPane.showMessageDialog(this, "No books selected."); return; }

        String[] options = { "Remove 1 copy / ID", "Remove ALL copies / ID", "Remove N copies / ID..." };
        int choice = JOptionPane.showOptionDialog(this, "Choose remove mode:", "Remove Options",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            if (JOptionPane.showConfirmDialog(this, "Remove one copy for each selected book?", "Confirm",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            for (int id : selectedIds) bookStorage.removeBookById(id);
            afterMutateRefresh();

        } else if (choice == 1) {
            if (JOptionPane.showConfirmDialog(this, "Remove ALL copies for each selected book?", "Confirm",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            for (int id : selectedIds) bookStorage.removeAllCopiesById(id);
            afterMutateRefresh();

        } else if (choice == 2) {
            String input = JOptionPane.showInputDialog(this, "Enter N (copies to remove per ID):", "1");
            if (input == null) return;
            try {
                int n = Integer.parseInt(input.trim());
                if (n <= 0) { JOptionPane.showMessageDialog(this, "N must be >= 1."); return; }
                if (JOptionPane.showConfirmDialog(this, "Remove " + n + " copies for each selected book?", "Confirm",
                        JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
                for (int id : selectedIds) bookStorage.removeCopiesById(id, n);
                afterMutateRefresh();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "N must be an integer.");
            }
        }
    }

    //edit books
    private void editSelectedBook() {
        int selectedModelRow = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (Boolean.TRUE.equals(model.getValueAt(i, COL_SELECT))) {
                if (selectedModelRow != -1) { JOptionPane.showMessageDialog(this, "Please select only one book to edit."); return; }
                selectedModelRow = i;
            }
        }
        if (selectedModelRow == -1) { JOptionPane.showMessageDialog(this, "Please select a book to edit."); return; }

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

        dialog.add(new JLabel("New ID:")); dialog.add(idField);
        dialog.add(new JLabel("Title:"));  dialog.add(titleField);
        dialog.add(new JLabel("Author:")); dialog.add(authorField);

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> {
            try {
                int newId = Integer.parseInt(idField.getText().trim());
                String newTitle = titleField.getText().trim();
                String newAuthor = authorField.getText().trim();
                if (newTitle.isEmpty() || newAuthor.isEmpty()) { JOptionPane.showMessageDialog(dialog, "All fields are required."); return; }

                Book src = bookStorage.findBookById(oldId);
                if (src == null) { JOptionPane.showMessageDialog(dialog, "Original book not found."); return; }

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
                            JOptionPane.showMessageDialog(dialog, "Target ID exists with a different Title/Author.\nAction blocked.");
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

    //quan li yeu cao muon
    private void showRequestsDialog() {
        JDialog dlg = new JDialog(this, "Pending Borrow Requests", true);
        dlg.setSize(820, 420);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(8, 8));

        DefaultTableModel rm = new DefaultTableModel(
                new Object[]{"Select","ReqID","ReaderID","BookID","Title","Author","Requested On"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 0; }
            @Override public Class<?> getColumnClass(int c) { return c==0 ? Boolean.class : Object.class; }
        };
        JTable tbl = new JTable(rm);
        tbl.setRowHeight(22);
        dlg.add(new JScrollPane(tbl), BorderLayout.CENTER);

        Runnable reload = () -> {
            rm.setRowCount(0);
            for (BorrowRequest r : requestStorage.getPending()) {
                rm.addRow(new Object[]{
                        false,
                        r.getRequestId(),
                        r.getReaderId(),
                        r.getBookId(),
                        r.getTitle(),
                        r.getAuthor(),
                        String.valueOf(r.getRequestDate())
                });
            }
        };
        reload.run();

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton approve = new JButton("Approve Selected");
        JButton reject  = new JButton("Reject Selected");
        JButton refresh = new JButton("Refresh");
        JButton close   = new JButton("Close");
        btns.add(approve); btns.add(reject); btns.add(refresh); btns.add(close);
        dlg.add(btns, BorderLayout.SOUTH);

        approve.addActionListener(e -> {
            java.util.List<Integer> reqIds = new ArrayList<>();
            for (int i=0;i<rm.getRowCount();i++) {
                if (Boolean.TRUE.equals(rm.getValueAt(i,0))) {
                    reqIds.add(((Number) rm.getValueAt(i,1)).intValue());
                }
            }
            if (reqIds.isEmpty()) { JOptionPane.showMessageDialog(dlg, "No requests selected."); return; }

            int ok = JOptionPane.showConfirmDialog(dlg, "Approve selected requests?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;

            int approved=0, failed=0;
            for (int id : reqIds) {
                boolean ok1 = requestStorage.approveRequest(id, borrowStorage);
                if (ok1) approved++; else failed++;
            }
            reload.run();
            refreshBookTable(); // cập nhật kho hiển thị
            JOptionPane.showMessageDialog(dlg,
                    "Approved: " + approved + (failed>0 ? (", Failed (no stock): " + failed) : ""));
        });

        reject.addActionListener(e -> {
            java.util.List<Integer> reqIds = new ArrayList<>();
            for (int i=0;i<rm.getRowCount();i++) {
                if (Boolean.TRUE.equals(rm.getValueAt(i,0))) {
                    reqIds.add(((Number) rm.getValueAt(i,1)).intValue());
                }
            }
            if (reqIds.isEmpty()) { JOptionPane.showMessageDialog(dlg, "No requests selected."); return; }

            int ok = JOptionPane.showConfirmDialog(dlg, "Reject selected requests?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;

            int rejected=0;
            for (int id : reqIds) if (requestStorage.rejectRequest(id)) rejected++;
            reload.run();
            JOptionPane.showMessageDialog(dlg, "Rejected: " + rejected);
        });

        refresh.addActionListener(e -> reload.run());
        close.addActionListener(e -> dlg.dispose());

        dlg.setVisible(true);
    }

    //logout
    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this, "Do you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame(new UserDatabase()));
    }

    private void afterMutateRefresh() { refreshBookTable(); }

    private boolean safeEqualsIgnoreCase(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equalsIgnoreCase(b);
    }

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
            public void actionPerformed(java.awt.event.ActionEvent e) { showAddBookDialog(); }
        });
        im.put(KeyStroke.getKeyStroke("DELETE"), "removeSelected");
        am.put("removeSelected", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { removeSelectedBooks(); }
        });
        im.put(KeyStroke.getKeyStroke("ENTER"), "editSelected");
        am.put("editSelected", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { editSelectedBook(); }
        });
    }
}
