import java.time.LocalDate;
import java.util.Scanner;

public class ReaderUser implements Reader {
    private int id;
    private String name;
    private BorrowStorage borrowStorage;
    private Scanner scanner;

    public ReaderUser(int id, String name, BorrowStorage borrowStorage) {
        this.id = id;
        this.name = name;
        this.borrowStorage = borrowStorage;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void borrowBook() {
        System.out.print("Enter book ID to borrow: ");
        int bookId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        LocalDate today = LocalDate.now();
        LocalDate returnDate = today.plusWeeks(2);

        borrowStorage.borrowBook(id, bookId, today, returnDate);
    }

    @Override
    public void returnBook() {
        System.out.print("Enter book ID to return: ");
        int bookId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        borrowStorage.returnBook(id, bookId);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
