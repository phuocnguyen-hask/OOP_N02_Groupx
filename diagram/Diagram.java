
public class Diagram {
    public static void main(String[] args){
        Librarian lib = new Librarian("admin", "1234", "Thu Thu", "092003");
        if (lib.login("admin", "1234")) {
            System.out.println("Login successful!");
            lib.qliSach();
            lib.qliNguoiDoc();
        }

        Book book = new Book("2910Ha2", "Lập trình Java", "Nguyễn Văn A", "CNTT", 5, "Available", 50000);
        book.addBook();

        BookReader reader = new BookReader("10023003", "Nam", "0123456789", 20);
        reader.borrowBook();

        Borrow tran = new Borrow("MKL2", reader.getID(), book.getBookID(), "2025-07-13", "2025-07-20");
        tran.createTransaction();
    }
}

