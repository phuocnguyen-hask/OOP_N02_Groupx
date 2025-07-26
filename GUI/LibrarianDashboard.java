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
        setSize(800, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Title
        JLabel title = new JLabel("Books in Library", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        // Table and model
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new Object[]{
            "ID", "Title", "Author", "Genre", "Quantity", "Status", "Price"
        });

        bookTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(bookTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshBookTable());
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load books initially
        refreshBookTable();

        setVisible(true);
    }

    private void refreshBookTable() {
        bookStorage.reloadBooksFromFile(); // reload from file
        ArrayList<Book> books = bookStorage.getBooks();

        // Clear existing rows
        model.setRowCount(0);

        // Add updated rows
        for (Book book : books) {
            model.addRow(new Object[]{
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
            });
        }
    }
}
