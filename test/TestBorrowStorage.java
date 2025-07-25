import java.time.LocalDate;

public class TestBorrowStorage {
    public static void main(String[] args){
        BookStorage bookStorage = new BookStorage();
        BorrowStorage bos = new BorrowStorage(bookStorage);
        System.out.println("Borrowed book: ");
        bos.showBorrowedByReader(10001);
    }
}
