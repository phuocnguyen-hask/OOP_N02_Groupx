public class TestUser {
    public static void main(String[] args){
        BookStorage bookStorage = new BookStorage();
        System.out.println("Old storage: ");
        bookStorage.showAllBooks();
        User lib = new User("Phuoc", "20230");
        lib.setLibrarianRole();
        lib.addBook(new Book(110, "statictics", "Streler"));
        System.out.println("New Storage: ");
        bookStorage.reloadBooksFromFile();
        bookStorage.showAllBooks();
    }
}
