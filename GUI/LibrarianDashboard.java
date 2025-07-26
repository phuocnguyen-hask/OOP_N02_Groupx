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
        add(new JScrollPane(bookTable), BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton refreshButton = new JButton("Refresh");
        JButton addBookButton = new JButton("Add Book");

        refreshButton.addActionListener(e -> refreshBookTable());
        addBookButton.addActionListener(e -> showAddBookForm());

        buttonPanel.add(refreshButton);
        buttonPanel.add(addBookButton);
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

    
}
