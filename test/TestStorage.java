public class TestStorage {
    public static void main(String[] args) throws Exception{
        BookStorage bookStorage = new BookStorage();
        bookStorage.saveBook();
        bookStorage.showBooks();
    }
}
