import java.io.Serializable;

public class User implements Serializable{
    private Reader reader;
    private Librarian lib;

    private int id;  // <-- unique reader ID
    private String userName;
    private String passWord;

    private BorrowStorage borrowStorage;  // <-- new

    public User(int id, String userName, String passWord, BorrowStorage borrowStorage) {
        this.id = id;
        this.userName = userName;
        this.passWord = passWord;
        this.borrowStorage = borrowStorage;

        this.reader = new NonReaderUser();
        this.lib = new NonLibrarianUser();
    }

    public String username() {
        return this.userName;
    }

    public String passWord() {
        return this.passWord;
    }
    public int getId(){
        return this.id;
    }

    public Librarian getLib(){
        return this.lib;
    }

    public Reader getReader(){
        return this.reader;
    }

    // Librarian functions
    public void addBook(Book book) {
        this.lib.addBook(book);
    }

    public void removeBook(int bookId) {
        this.lib.removeBook(bookId);
    }

    public void showAllBooks() {
        this.lib.showAllBooks();
    }

    public void manageUser() {
        // To implement
    }

    // Reader functions
    public void borrowBook() {
        this.reader.borrowBook();
    }

    public void returnBook() {
        this.reader.returnBook();
    }

    public void setLibrarianRole() {
        this.lib = new LibrarianUser();  // or pass BookStorage if needed
        this.reader = new NonReaderUser();
    }

    public void setReaderRole() {
        this.reader = new ReaderUser(id, userName, borrowStorage);  // ✅ now valid
        this.lib = new NonLibrarianUser();
    }
}
