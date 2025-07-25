import java.time.LocalDate;

public class TestBorrowBook {
    public static void main(String[] args){
        Book book = new Book(101, "Stats", "Joseph Blitzstein");
        BorrowBook boo = new BorrowBook(10 ,book, LocalDate.of(2025, 7, 25), LocalDate.of(2025, 9, 25));
        System.out.println(boo.toString());
    }
    
}
