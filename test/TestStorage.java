public class TestStorage {
    public static void main(String[] args) throws Exception{
        BookStorage bookStorage = new BookStorage();
        bookStorage.showAllBooks();
        bookStorage.addBook(new Book(101, "Dog in Fute", "Nkai"));
        bookStorage.showAllBooks();
    }
}
