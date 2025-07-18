import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BookManager {
    private List<Book> books = new ArrayList<>();

    public void addBook(Book b) {
        books.add(b);
    }

    public boolean deleteBook(String bookID) {
        return books.removeIf(b -> b.getBookID().equals(bookID));
    }

    public boolean editBook(String bookID, String newTitle, String newAuthor, String newCategory, int newQuant, String newStatus, double newPrice) {
        for (Book b : books) {
            if (b.getBookID().equals(bookID)) {
                // Setters bạn cần thêm trong class Book
                b.setTitle(newTitle);
                b.setAuthor(newAuthor);
                b.setCategory(newCategory);
                b.setQuant(newQuant);
                b.setStatus(newStatus);
                b.setPrice(newPrice);
                return true;
            }
        }
        return false;
    }

    public List<Book> listBooks() {
        return books;
    }

    public List<Book> filterByCategory(String category) {
        return books.stream()
                .filter(b -> b.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }
}
