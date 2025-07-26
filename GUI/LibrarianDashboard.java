import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class LibrarianDashboard extends JFrame {
    private User user;
    private BookStorage bookStorage;
    private DefaultTableModel model;
    private JTable bookTable;

    public LibrarianDashboard(User user) {
        this.user = user;
        this.bookStorage = user.getStorage();

        setTitle("Librarian Dashboard - " + user.username());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Title
        JLabel title = new JLabel("Books in Library", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        // Table setup
        model = new DefaultTableModel(new Object[]{"Select", "ID", "Title", "Author"}, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : super.getColumnClass(column);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // Only checkbox column is editable
            }
        };

        bookTable = new JTable(model);
        bookTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(bookTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Left side: Select All Checkbox
        JCheckBox selectAllBox = new JCheckBox("Select All");
        selectAllBox.addActionListener(e -> {
            boolean selected = selectAllBox.isSelected();
            for (int i = 0; i < model.getRowCount(); i++) {
                model.setValueAt(selected, i, 0);
            }
        });
        bottomPanel.add(selectAllBox, BorderLayout.WEST);

        // Right side: Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Add Book");
        JButton removeButton = new JButton("Remove Selected");

        addButton.addActionListener(e -> showAddBookForm());
        removeButton.addActionListener(e -> removeCheckedBooks());

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        refreshBookTable();
        setVisible(true);
    }

    private void refreshBookTable() {
        bookStorage.reloadBooksFromFile();
        ArrayList<Book> books = bookStorage.getBooks();

        model.setRowCount(0);
        for (Book book : books) {
            model.addRow(new Object[]{false, book.getId(), book.getTitle(), book.getAuthor()});
        }
    }

    private void showAddBookForm() {
        JDialog dialog = new JDialog(this, "Add New Book", true);
        dialog.setSize(300, 250);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));
        dialog.setLocationRelativeTo(this);

        JTextField idField = new JTextField();
        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();

        dialog.add(new JLabel("ID:"));
        dialog.add(idField);
        dialog.add(new JLabel("Title:"));
        dialog.add(titleField);
        dialog.add(new JLabel("Author:"));
        dialog.add(authorField);

        JButton submitButton = new JButton("Add Book");
        submitButton.addActionListener(e -> {
            try {
                String idText = idField.getText().trim();
                String title = titleField.getText().trim();
                String author = authorField.getText().trim();

                if (idText.isEmpty() || title.isEmpty() || author.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill in all fields.");
                    return;
                }

                int id = Integer.parseInt(idText);

                Book newBook = new Book(id, title, author);
                bookStorage.addBook(newBook);
                refreshBookTable();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "ID must be an integer.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        dialog.add(new JLabel());  // spacing
        dialog.add(submitButton);
        dialog.setVisible(true);
    }

    private void removeCheckedBooks() {
        ArrayList<Book> toRemove = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            Boolean isChecked = (Boolean) model.getValueAt(i, 0);
            if (Boolean.TRUE.equals(isChecked)) {
                int id = (int) model.getValueAt(i, 1);
                for (Book book : bookStorage.getBooks()) {
                    if (book.getId() == id) {
                        toRemove.add(book);
                        break;
                    }
                }
            }
        }

        if (toRemove.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No books selected.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the selected books?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        for (Book book : toRemove) {
            bookStorage.removeBook(book);
        }

        refreshBookTable();
    }
}
