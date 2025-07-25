import java.time.LocalDate;

public class TestBorrowStorage {
    public static void main(String[] args){
        BookStorage bookStorage = new BookStorage();
        System.out.println("Book Storage: ");
        bookStorage.showAllBooks();
        BorrowStorage bos = new BorrowStorage(bookStorage);
        bos.borrowBook(10, 102, LocalDate.of(2025, 5, 25), LocalDate.of(2025, 12, 25));
        System.out.println("Borrowed book: ");
        bos.showBorrowedByReader(10);
    }
}
