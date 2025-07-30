import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import javax.swing.RowFilter;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class ReaderDashboard extends JFrame {
    private final User user;
    private final BookStorage bookStorage;
    private final BorrowStorage borrowStorage;

    private JTable bookTable;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    // LIVE SEARCH UI
    private JTextField searchField;
    private JComboBox<String> scopeCombo; // Title / Author / Both

    public ReaderDashboard(User user) {
        this.user = user;

        // Bảo đảm luôn có storage hợp lệ (trường hợp user cũ load từ file)
        BookStorage bs = (user.getStorage() != null) ? user.getStorage() : new BookStorage();
        BorrowStorage bws = (user.getBorrowStorage() != null) ? user.getBorrowStorage() : new BorrowStorage(bs);
        user.setBorrowStorage(bws);

        this.bookStorage  = bs;
        this.borrowStorage = bws;

        setTitle("Reader Dashboard - " + user.username());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 540);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        /* ===== TOP: Title + Search ===== */
        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Welcome, " + user.username() + " — Available Books", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(title, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        searchField = new JTextField(26);
        scopeCombo = new JComboBox<>(new String[]{"Title", "Author", "Both"});
        scopeCombo.setSelectedItem("Both"); // mặc định tìm cả 2
        JButton clearBtn  = new JButton("Clear");

        searchPanel.add(new JLabel("Query:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("In:"));
        searchPanel.add(scopeCombo);
        searchPanel.add(clearBtn);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        /* ===== CENTER: Table (ID, Title, Author, Quantity) ===== */
        model = new DefaultTableModel(new Object[]{"ID", "Title", "Author", "Quantity"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0, 3 -> Integer.class;
                    default -> String.class;
                };
            }
        };
        bookTable = new JTable(model);
        bookTable.setRowHeight(24);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        sorter = new TableRowSorter<>(model);
        bookTable.setRowSorter(sorter);

        // căn giữa cột ID, Quantity
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        bookTable.getColumnModel().getColumn(0).setCellRenderer(center);
        bookTable.getColumnModel().getColumn(3).setCellRenderer(center);

        add(new JScrollPane(bookTable), BorderLayout.CENTER);

        /* ===== SOUTH: Buttons ===== */
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton myBorrowedBtn = new JButton("My Borrowed");
        JButton borrowBtn  = new JButton("Borrow Selected");
        JButton returnBtn  = new JButton("Return by ID...");
        JButton refreshBtn = new JButton("Refresh");
        JButton logoutBtn  = new JButton("Logout");

        btnPanel.add(myBorrowedBtn);
        btnPanel.add(borrowBtn);
        btnPanel.add(returnBtn);
        btnPanel.add(refreshBtn);
        btnPanel.add(logoutBtn);

        add(btnPanel, BorderLayout.SOUTH);

        /* ===== LIVE SEARCH Events ===== */
        // 1) Gõ tới đâu lọc tới đó
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void apply() { applyFilter(); }
            public void insertUpdate(DocumentEvent e) { apply(); }
            public void removeUpdate(DocumentEvent e) { apply(); }
            public void changedUpdate(DocumentEvent e) { apply(); }
        });

        // 2) Đổi phạm vi lọc (Title/Author/Both) → re-apply ngay
        scopeCombo.addActionListener(e -> applyFilter());

        // 3) Clear search
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            sorter.setRowFilter(null);
        });

        /* ===== Actions ===== */
        myBorrowedBtn.addActionListener(e -> showMyBorrowedDialog());
        borrowBtn.addActionListener(e -> borrowSelectedOneCopy());
        returnBtn.addActionListener(e -> returnByIdDialog());
        refreshBtn.addActionListener(e -> refreshAvailableBooks());
        logoutBtn.addActionListener(e -> doLogout());

        // double-click để mượn nhanh
        bookTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && bookTable.getSelectedRow() != -1) {
                    borrowSelectedOneCopy();
                }
            }
        });

        // phím tắt
        setupShortcuts();

        // lần đầu load
        refreshAvailableBooks();
        setVisible(true);
    }

    /** Áp dụng filter theo text + phạm vi (Title/Author/Both) — LIVE SEARCH */
    private void applyFilter() {
        if (sorter == null) return;
        String q = searchField.getText().trim();
        if (q.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        String pat = "(?i)" + Pattern.quote(q); // không phân biệt hoa thường
        String scope = (String) scopeCombo.getSelectedItem();
        if ("Author".equals(scope)) {
            sorter.setRowFilter(RowFilter.regexFilter(pat, 2)); // cột 2 = Author
        } else if ("Both".equals(scope)) {
            List<RowFilter<Object,Object>> ors = new ArrayList<>();
            ors.add(RowFilter.regexFilter(pat, 1)); // Title
            ors.add(RowFilter.regexFilter(pat, 2)); // Author
            sorter.setRowFilter(RowFilter.orFilter(ors));
        } else {
            sorter.setRowFilter(RowFilter.regexFilter(pat, 1)); // Title
        }
    }

    /** Gom sách theo ID để hiển thị Quantity (đếm số bản trong storage) */
    private void refreshAvailableBooks() {
        model.setRowCount(0);
        bookStorage.reloadBooksFromFile();

        List<Book> all = bookStorage.getBooks();
        if (all == null || all.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }

        Map<Integer, Integer> qtyMap = new LinkedHashMap<>();
        Map<Integer, Book> refMap = new LinkedHashMap<>();

        for (Book b : all) {
            int id = b.getId();
            qtyMap.put(id, qtyMap.getOrDefault(id, 0) + 1);
            refMap.putIfAbsent(id, b); // giữ 1 quyển làm thông tin
        }

        for (Map.Entry<Integer, Integer> e : qtyMap.entrySet()) {
            int id = e.getKey();
            int qty = e.getValue();
            Book ref = refMap.get(id);
            model.addRow(new Object[]{ id, ref.getTitle(), ref.getAuthor(), qty });
        }

        // GIỮ LẠI LIVE FILTER SAU KHI REFRESH
        applyFilter();
    }

    /** Mượn 1 bản của ID đang chọn */
    private void borrowSelectedOneCopy() {
        int viewRow = bookTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to borrow.");
            return;
        }
        int modelRow = bookTable.convertRowIndexToModel(viewRow);
        int bookId = ((Number) model.getValueAt(modelRow, 0)).intValue();
        int available = ((Number) model.getValueAt(modelRow, 3)).intValue();
        if (available <= 0) {
            JOptionPane.showMessageDialog(this, "This book is currently unavailable.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Borrow 1 copy of book ID " + bookId + "?",
                "Confirm Borrow",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            LocalDate borrowDate = LocalDate.now();
            LocalDate returnDate = borrowDate.plusDays(14);
            borrowStorage.borrowBook(user.getId(), bookId, borrowDate, returnDate);
            refreshAvailableBooks();
            JOptionPane.showMessageDialog(this, "Borrowed successfully! Due date: " + returnDate);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Borrow failed: " + ex.getMessage());
        }
    }

    /** Trả sách theo ID (nhập ID) */
    private void returnByIdDialog() {
        String input = JOptionPane.showInputDialog(this, "Enter Book ID to return:");
        if (input == null) return; // cancel
        try {
            int bookId = Integer.parseInt(input.trim());
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Return 1 copy of book ID " + bookId + "?",
                    "Confirm Return",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            borrowStorage.returnBook(user.getId(), bookId);
            refreshAvailableBooks();
            JOptionPane.showMessageDialog(this, "Returned successfully.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Book ID must be an integer.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Return failed: " + ex.getMessage());
        }
    }

    /** Hộp thoại hiển thị danh sách sách đang mượn + Return Selected */
    private void showMyBorrowedDialog() {
        java.util.List<BorrowBook> borrowed = borrowStorage.getBorrowedByReader(user.getId());
        JDialog dialog = new JDialog(this, "My Borrowed Books", true);
        dialog.setSize(720, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(8, 8));

        DefaultTableModel bm = new DefaultTableModel(
                new Object[]{"Book ID", "Title", "Author", "Borrow Date", "Due Date"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0 -> Integer.class;
                    default -> String.class;
                };
            }
        };
        JTable tbl = new JTable(bm);
        tbl.setRowHeight(22);
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        tbl.getColumnModel().getColumn(0).setCellRenderer(center);

        for (BorrowBook bb : borrowed) {
            Book bk = bb.getBook();
            String bd = String.valueOf(bb.getBorrowDate());
            String rd = String.valueOf(bb.getReturnDate());
            bm.addRow(new Object[]{ bk.getId(), bk.getTitle(), bk.getAuthor(), bd, rd });
        }

        dialog.add(new JScrollPane(tbl), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton returnSelected = new JButton("Return Selected");
        JButton close = new JButton("Close");
        buttons.add(returnSelected);
        buttons.add(close);
        dialog.add(buttons, BorderLayout.SOUTH);

        returnSelected.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(dialog, "Please select a row to return.");
                return;
            }
            int bookId = ((Number) bm.getValueAt(row, 0)).intValue();
            int confirm = JOptionPane.showConfirmDialog(
                    dialog, "Return 1 copy of book ID " + bookId + " ?", "Confirm Return",
                    JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            borrowStorage.returnBook(user.getId(), bookId);
            bm.removeRow(row);
            refreshAvailableBooks();
            JOptionPane.showMessageDialog(dialog, "Returned successfully.");
        });

        close.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    /** Đăng xuất về màn hình Login */
    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this, "Do you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame(new UserDatabase()));
    }

    /** Phím tắt */
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

        im.put(KeyStroke.getKeyStroke("ENTER"), "borrowSelected");
        am.put("borrowSelected", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                borrowSelectedOneCopy();
            }
        });

        im.put(KeyStroke.getKeyStroke("control R"), "returnById");
        am.put("returnById", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                returnByIdDialog();
            }
        });

        im.put(KeyStroke.getKeyStroke("F5"), "refreshBooks");
        am.put("refreshBooks", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                refreshAvailableBooks();
            }
        });

        im.put(KeyStroke.getKeyStroke("control B"), "myBorrowed");
        am.put("myBorrowed", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                showMyBorrowedDialog();
            }
        });
    }
}
