import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new Object[]{"ID", "Title", "Author"});

        bookTable = new JTable(model);
        bookTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        add(new JScrollPane(bookTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Add Book");
        JButton removeButton = new JButton("Remove Book");

        addButton.addActionListener(e -> showAddBookForm());
        removeButton.addActionListener(e -> removeSelectedBooks(bookTable));

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        add(buttonPanel, BorderLayout.SOUTH);


        refreshBookTable();
        setVisible(true);
    }

    private void refreshBookTable() {
        bookStorage.reloadBooksFromFile();
        ArrayList<Book> books = bookStorage.getBooks();

        model.setRowCount(0);
        for (Book book : books) {
            model.addRow(new Object[]{
                book.getId(),
                book.getTitle(),
                book.getAuthor()
            });
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

                int id = Integer.parseInt(idText);  // now parsed as int

                Book newBook = new Book(id, title, author);
                bookStorage.addBook(newBook); // persist to file
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

    private void removeSelectedBooks(JTable table) {
        int[] selectedRows = table.getSelectedRows();

        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "No books selected.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete selected books?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        ArrayList<Book> booksToRemove = new ArrayList<>();
        for (int row : selectedRows) {
            int modelRow = table.convertRowIndexToModel(row);
            int id = (int) table.getModel().getValueAt(modelRow, 0);

            for (Book b : bookStorage.getBooks()) {
                if (b.getId() == id) {
                    booksToRemove.add(b);
                    break;
                }
            }
        }

        for (Book b : booksToRemove) {
            bookStorage.removeBook(b);
        }
        refreshBookTable();
    }

    
}
