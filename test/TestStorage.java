import java.io.IOException;

public class TestStorage {
    public static void main(String[] args) throws IOException, ClassNotFoundException{
        BookStorage bookStorage = new BookStorage();
        Book newBook = new Book(230, "AtCat", "AssinCat", 20);
        bookStorage.addBook(newBook);
        bookStorage.saveBook();
        System.out.println(bookStorage.getBook(230).toString());
    }
}
