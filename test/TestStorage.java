public class TestStorage {
    public static void main(String[] args) throws Exception{
        BookStorage bookStorage = new BookStorage();
        bookStorage.showAllBooks();
        bookStorage.removeBookById(1102);
        bookStorage.showAllBooks();
    }
}
