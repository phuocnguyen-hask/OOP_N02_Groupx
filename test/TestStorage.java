public class TestStorage {
    public static void main(String[] args) throws Exception{
        BookStorage bookStorage = new BookStorage();
        Book newBook = new Book(120, "Dogm", "Dogusai", 12);
        bookStorage.addBook(newBook);
        bookStorage.saveBook();
        System.out.println(bookStorage.getQuant());
        System.out.println(bookStorage.getBook(230).toString());
    }
}
