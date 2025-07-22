public class TestBorrowStorage {
    public static void main(String[] args){
        BookStorage bookStorage = new BookStorage();
        System.out.println("Book Storage: ");
        bookStorage.showAllBooks();
        BorrowStorage borrowStorage = new BorrowStorage();
        borrowStorage.addBorrowedBooksByID(101, 1);
        System.out.println("Borrowed Book: ");
        borrowStorage.showAllBorrowedBook();
        bookStorage.reloadBooksFromFile();
        System.out.println("Book Storage: ");
        bookStorage.showAllBooks();
    }
}
