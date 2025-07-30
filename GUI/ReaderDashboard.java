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

    // nơi lưu yêu cầu mượn
    private final RequestStorage requestStorage = new RequestStorage();

    private JTable bookTable;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    // LIVE SEARCH UI
    private JTextField searchField;
    private JComboBox<String> scopeCombo; // Title / Author / Both

    public ReaderDashboard(User user) {
        this.user = user;

        // Bảo đảm luôn có storage hợp lệ
        BookStorage bs = (user.getStorage() != null) ? user.getStorage() : new BookStorage();
        BorrowStorage bws = (user.getBorrowStorage() != null) ? user.getBorrowStorage() : new BorrowStorage(bs);
        user.setBorrowStorage(bws);

        this.bookStorage  = bs;
        this.borrowStorage = bws;

        setTitle("Reader Dashboard - " + user.username());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 560);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        /* TOP: Title + Search */
        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Welcome, " + user.username() + " — Available Books", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(title, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        searchField = new JTextField(26);
        scopeCombo = new JComboBox<>(new String[]{"Title", "Author", "Both"});
        scopeCombo.setSelectedItem("Both");
        JButton clearBtn  = new JButton("Clear");

        searchPanel.add(new JLabel("Query:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("In:"));
        searchPanel.add(scopeCombo);
        searchPanel.add(clearBtn);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        /* CENTER: Table (ID, Title, Author, Quantity) */
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

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        bookTable.getColumnModel().getColumn(0).setCellRenderer(center);
        bookTable.getColumnModel().getColumn(3).setCellRenderer(center);

        add(new JScrollPane(bookTable), BorderLayout.CENTER);

        /* SOUTH: Buttons */
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton myBorrowedBtn = new JButton("My Borrowed");
        JButton myRequestsBtn = new JButton("My Requests"); // NEW
        JButton requestBtn = new JButton("Request Borrow");
        JButton returnBtn  = new JButton("Return by ID...");
        JButton refreshBtn = new JButton("Refresh");
        JButton logoutBtn  = new JButton("Logout");

        btnPanel.add(myBorrowedBtn);
        btnPanel.add(myRequestsBtn);  // NEW
        btnPanel.add(requestBtn);
        btnPanel.add(returnBtn);
        btnPanel.add(refreshBtn);
        btnPanel.add(logoutBtn);

        add(btnPanel, BorderLayout.SOUTH);

        /* LIVE SEARCH */
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void apply() { applyFilter(); }
            public void insertUpdate(DocumentEvent e) { apply(); }
            public void removeUpdate(DocumentEvent e) { apply(); }
            public void changedUpdate(DocumentEvent e) { apply(); }
        });
        scopeCombo.addActionListener(e -> applyFilter());
        clearBtn.addActionListener(e -> { searchField.setText(""); sorter.setRowFilter(null); });

        /* Actions */
        myBorrowedBtn.addActionListener(e -> showMyBorrowedDialog());
        myRequestsBtn.addActionListener(e -> showMyPendingRequestsDialog()); // NEW
        requestBtn.addActionListener(e -> sendBorrowRequest());
        returnBtn.addActionListener(e -> returnByIdDialog());
        refreshBtn.addActionListener(e -> refreshAvailableBooks());
        logoutBtn.addActionListener(e -> doLogout());

        // double click → gửi request nhanh
        bookTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && bookTable.getSelectedRow() != -1) {
                    sendBorrowRequest();
                }
            }
        });

        setupShortcuts();
        refreshAvailableBooks();
        setVisible(true);
    }

    /* LIVE FILTER */
    private void applyFilter() {
        if (sorter == null) return;
        String q = searchField.getText().trim();
        if (q.isEmpty()) { sorter.setRowFilter(null); return; }
        String pat = "(?i)" + Pattern.quote(q);
        String scope = (String) scopeCombo.getSelectedItem();
        if ("Author".equals(scope)) {
            sorter.setRowFilter(RowFilter.regexFilter(pat, 2));
        } else if ("Both".equals(scope)) {
            List<RowFilter<Object,Object>> ors = new ArrayList<>();
            ors.add(RowFilter.regexFilter(pat, 1));
            ors.add(RowFilter.regexFilter(pat, 2));
            sorter.setRowFilter(RowFilter.orFilter(ors));
        } else {
            sorter.setRowFilter(RowFilter.regexFilter(pat, 1));
        }
    }

    /** Gom theo ID để hiển thị Quantity */
    private void refreshAvailableBooks() {
        model.setRowCount(0);
        bookStorage.reloadBooksFromFile();

        List<Book> all = bookStorage.getBooks();
        if (all == null || all.isEmpty()) { sorter.setRowFilter(null); return; }

        Map<Integer, Integer> qtyMap = new LinkedHashMap<>();
        Map<Integer, Book> refMap = new LinkedHashMap<>();
        for (Book b : all) {
            int id = b.getId();
            qtyMap.put(id, qtyMap.getOrDefault(id, 0) + 1);
            refMap.putIfAbsent(id, b);
        }
        for (Map.Entry<Integer, Integer> e : qtyMap.entrySet()) {
            int id = e.getKey();
            int qty = e.getValue();
            Book ref = refMap.get(id);
            model.addRow(new Object[]{ id, ref.getTitle(), ref.getAuthor(), qty });
        }
        applyFilter(); // giữ filter
    }

    /** Gửi yêu cầu mượn tới librarian */
    private void sendBorrowRequest() {
        int viewRow = bookTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to request.");
            return;
        }
        int modelRow = bookTable.convertRowIndexToModel(viewRow);
        int bookId   = ((Number) model.getValueAt(modelRow, 0)).intValue();
        String title = String.valueOf(model.getValueAt(modelRow, 1));
        String author= String.valueOf(model.getValueAt(modelRow, 2));
        int available = ((Number) model.getValueAt(modelRow, 3)).intValue();
        if (available <= 0) {
            JOptionPane.showMessageDialog(this, "This book is currently unavailable.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this, "Send borrow request for book ID " + bookId + " ?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int reqId = requestStorage.nextId();
        BorrowRequest req = new BorrowRequest(
                reqId, user.getId(), bookId, title, author, LocalDate.now()
        );
        requestStorage.addRequest(req);
        JOptionPane.showMessageDialog(this, "Request sent! (ID: " + reqId + "). Please wait for approval.");
    }

    /* Trả 1 bản theo ID */
    private void returnByIdDialog() {
        String input = JOptionPane.showInputDialog(this, "Enter Book ID to return:");
        if (input == null) return;
        try {
            int bookId = Integer.parseInt(input.trim());
            int confirm = JOptionPane.showConfirmDialog(
                    this, "Return 1 copy of book ID " + bookId + " ?", "Confirm",
                    JOptionPane.YES_NO_OPTION);
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

    /** Danh sách sách đang mượn */
    private void showMyBorrowedDialog() {
        borrowStorage.reloadBorrowListFromFilePublic();   // đảm bảo dữ liệu mới
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
            bm.addRow(new Object[]{ bk.getId(), bk.getTitle(), bk.getAuthor(),
                    String.valueOf(bb.getBorrowDate()), String.valueOf(bb.getReturnDate()) });
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
                    dialog, "Return 1 copy of book ID " + bookId + " ?", "Confirm",
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

    /** NEW: Xem các yêu cầu mượn CHƯA duyệt của chính mình */
    private void showMyPendingRequestsDialog() {
        // đảm bảo đọc dữ liệu mới nhất từ file (nếu có)
        try { requestStorage.reloadFromFilePublic(); } catch (Throwable ignore) {}

        List<BorrowRequest> pending = requestStorage.getPendingByReader(user.getId());

        JDialog dialog = new JDialog(this, "My Pending Requests", true);
        dialog.setSize(760, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(8, 8));

        DefaultTableModel rm = new DefaultTableModel(
                new Object[]{"Req ID", "Book ID", "Title", "Author", "Requested At", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0, 1 -> Integer.class;
                    default -> String.class;
                };
            }
        };
        JTable tbl = new JTable(rm);
        tbl.setRowHeight(22);
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        tbl.getColumnModel().getColumn(0).setCellRenderer(center);
        tbl.getColumnModel().getColumn(1).setCellRenderer(center);

        for (BorrowRequest r : pending) {
            rm.addRow(new Object[]{
                    r.getRequestId(), r.getBookId(), r.getTitle(), r.getAuthor(),
                    String.valueOf(r.getRequestDate()), String.valueOf(r.getStatus())
            });
        }

        dialog.add(new JScrollPane(tbl), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton close = new JButton("Close");
        buttons.add(close);
        dialog.add(buttons, BorderLayout.SOUTH);

        close.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this, "Do you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame(new UserDatabase()));
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
        im.put(KeyStroke.getKeyStroke("ENTER"), "requestBorrow");
        am.put("requestBorrow", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { sendBorrowRequest(); }
        });
        im.put(KeyStroke.getKeyStroke("control R"), "returnById");
        am.put("returnById", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { returnByIdDialog(); }
        });
        im.put(KeyStroke.getKeyStroke("F5"), "refreshBooks");
        am.put("refreshBooks", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { refreshAvailableBooks(); }
        });
        im.put(KeyStroke.getKeyStroke("control B"), "myBorrowed");
        am.put("myBorrowed", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { showMyBorrowedDialog(); }
        });
        im.put(KeyStroke.getKeyStroke("control Q"), "myRequests");
        am.put("myRequests", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { showMyPendingRequestsDialog(); }
        });
    }
}
