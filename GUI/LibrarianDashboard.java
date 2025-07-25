import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class LibrarianDashboard extends JFrame {
    private User user;
    private BookStorage bookStorage;

    public LibrarianDashboard(User user) {
        this.user = user;
        this.bookStorage = user.getLib().getStorage(); // assuming you use getLib() for librarian

        setTitle("Librarian Dashboard - " + user.username());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);
        setLocationRelativeTo(null);

        // Title
        JLabel title = new JLabel("Books in Library", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        // Table
        JTable bookTable = new JTable();
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(new Object[]{
            "ID", "Title", "Author", "Genre", "Quantity", "Status", "Price"
        });

        ArrayList<Book> books = bookStorage.getBooks();
        for (Book book : books) {
            model.addRow(new Object[]{
                book.getId(),
                book.getTitle(),
                book.getAuthor()
            });
        }

        bookTable.setModel(model);
        JScrollPane scrollPane = new JScrollPane(bookTable);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }
}
