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
        this.bookStorage = user.getStorage();

        setTitle("Librarian Dashboard - " + user.username());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);
        setLocationRelativeTo(null);

        // Title
        JLabel title = new JLabel("Books in Library", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        // Table
        bookTable = new JTable();
        model = new DefaultTableModel(new Object[]{"Select", "ID", "Title", "Author"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : super.getColumnClass(columnIndex);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // only checkbox editable
            }
        };
        bookTable.setModel(model);
        JScrollPane scrollPane = new JScrollPane(bookTable);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton refreshButton = new JButton("Refresh");
        JButton addButton = new JButton("Add Book");
        JButton removeButton = new JButton("Remove Selected");
        JButton editButton = new JButton("Edit Selected");

        refreshButton.addActionListener(e -> refreshBookTable());
        addButton.addActionListener(e -> showAddBookDialog());
        removeButton.addActionListener(e -> removeSelectedBooks());
        editButton.addActionListener(e -> editSelectedBook());

        buttonPanel.add(refreshButton);
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(editButton);
        add(buttonPanel, BorderLayout.SOUTH);

        refreshBookTable();
        setVisible(true);
    }

    private void refreshBookTable() {
        model.setRowCount(0);
        bookStorage.reloadBooksFromFile();
        for (Book book : bookStorage.getBooks()) {
            model.addRow(new Object[]{false, book.getId(), book.getTitle(), book.getAuthor()});
        }
    }

    private void showAddBookDialog() {
        JDialog dialog = new JDialog(this, "Add New Book", true);
        dialog.setSize(300, 200);
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

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String title = titleField.getText().trim();
                String author = authorField.getText().trim();

                if (title.isEmpty() || author.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "All fields are required.");
                    return;
                }

                Book newBook = new Book(id, title, author);
                bookStorage.addBook(newBook);
                refreshBookTable();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "ID must be an integer.");
            }
        });

        dialog.add(new JLabel());
        dialog.add(saveButton);
        dialog.setVisible(true);
    }

    private void removeSelectedBooks() {
        ArrayList<Book> toRemove = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            boolean selected = Boolean.TRUE.equals(model.getValueAt(i, 0));
            if (selected) {
                int id = (int) model.getValueAt(i, 1);
                for (Book book : bookStorage.getBooks()) {
                    if (book.getId() == id) {
                        toRemove.add(book);
                        break;
                    }
                }
            }
        }

        for (Book b : toRemove) {
            bookStorage.removeBook(b);
        }

        refreshBookTable();
    }

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

        int oldId = (int) model.getValueAt(selectedRow, 1);
        String oldTitle = (String) model.getValueAt(selectedRow, 2);
        String oldAuthor = (String) model.getValueAt(selectedRow, 3);

        JDialog dialog = new JDialog(this, "Edit Book", true);
        dialog.setSize(300, 250);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));
        dialog.setLocationRelativeTo(this);

        JTextField idField = new JTextField(String.valueOf(oldId));
        JTextField titleField = new JTextField(oldTitle);
        JTextField authorField = new JTextField(oldAuthor);

        dialog.add(new JLabel("ID:"));
        dialog.add(idField);
        dialog.add(new JLabel("Title:"));
        dialog.add(titleField);
        dialog.add(new JLabel("Author:"));
        dialog.add(authorField);

        JButton updateButton = new JButton("Update Book");
        updateButton.addActionListener(e -> {
            try {
                String idText = idField.getText().trim();
                String title = titleField.getText().trim();
                String author = authorField.getText().trim();

                if (idText.isEmpty() || title.isEmpty() || author.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill in all fields.");
                    return;
                }

                int newId = Integer.parseInt(idText);

                for (Book b : new ArrayList<>(bookStorage.getBooks())) {
                    if (b.getId() == oldId) {
                        bookStorage.removeBook(b);
                        break;
                    }
                }

                Book updatedBook = new Book(newId, title, author);
                bookStorage.addBook(updatedBook);

                refreshBookTable();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "ID must be an integer.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        dialog.add(new JLabel());
        dialog.add(updateButton);
        dialog.setVisible(true);
    }
}
