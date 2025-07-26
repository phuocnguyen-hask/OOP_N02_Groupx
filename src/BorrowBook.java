import java.io.Serializable;
import java.time.LocalDate;

public class BorrowBook implements Serializable {
    private static final long serialVersionUID = 1L;

    private int readerId;
    private Book book;
    private LocalDate borrowDate;
    private LocalDate returnDate;

    public BorrowBook(int readerId, Book book, LocalDate borrowDate, LocalDate returnDate) {
        this.readerId = readerId;
        this.book = book;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
    }

    public int getReaderId() {
        return readerId;
    }

    public Book getBook() {
        return book;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public String toString() {
        return "Borrowed by Reader " + readerId +
                " | Book: " + book +
                " | Borrow Date: " + borrowDate +
                " | Return Date: " + returnDate;
    }
}
