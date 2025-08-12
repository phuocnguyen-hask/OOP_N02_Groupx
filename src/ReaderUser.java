import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class ReaderUser implements Reader, Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String phoneNumber;  // Thêm số điện thoại
    private BorrowStorage borrowStorage;
    private transient Scanner scanner;

    public ReaderUser(int id, String name, String phoneNumber, BorrowStorage borrowStorage) {
        this.id = id;
        this.name = name;
        this.borrowStorage = borrowStorage;
        this.phoneNumber = phoneNumber;
        this.scanner = new Scanner(System.in);
    }

    // getter cho phoneNumber
    public String getPhoneNumber() {
        return phoneNumber;
    }

    // setter cho phoneNumber
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    //method moi de lay tt sach dang muon
    public List<BorrowBook> getBorrowedBooks() {
        if (borrowStorage == null) return Collections.emptyList();
        return borrowStorage.listBorrowedByReader(id);
    }
}
