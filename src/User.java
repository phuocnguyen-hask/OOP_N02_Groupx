import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private transient Reader reader;
    private transient Librarian lib;

    private int id;
    private String userName;
    private String phoneNumber;
    private String passWord;

    public enum Role { READER, LIBRARIAN }
    private Role role = Role.READER;  
    private BorrowStorage borrowStorage;

    public User(int id, String userName, String passWord, String phoneNumber, BorrowStorage borrowStorage) {
        this.id = id;
        this.userName = userName;
        this.passWord = passWord;
        this.phoneNumber = phoneNumber;
        this.borrowStorage = borrowStorage;

        this.reader = new NonReaderUser();
        this.lib = new NonLibrarianUser();
    }

    // ====== Getters ======
    public String username() { return this.userName; }
    public String passWord() { return this.passWord; }
    public int getId()       { return this.id; }
    public Librarian getLib(){ return this.lib; }
    public Reader getReader(){ return this.reader; }



    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    public boolean isLibrarian() { return role == Role.LIBRARIAN; }
    public Role getRole() { return role; }

    // ====== BorrowStorage ======
    public BorrowStorage getBorrowStorage() { return this.borrowStorage; }
    public void setBorrowStorage(BorrowStorage borrowStorage) { this.borrowStorage = borrowStorage; }

    public BookStorage getStorage() {
        if (this.borrowStorage != null) return this.borrowStorage.getBookStorage();
        if (this.lib != null) return this.lib.getStorage();
        return null;
    }

    public void addBook(Book book) { this.lib.addBook(book); }
    public void removeBook(int bookId) { this.lib.removeBook(bookId); }
    public void showAllBooks() { this.lib.showAllBooks(); }

    public void manageUser() {
        // khong biet cho cai gi vao day
    }
    public void borrowBook() { this.reader.borrowBook(); }
    public void returnBook() { this.reader.returnBook(); }

    public void setLibrarianRole() {
        this.role = Role.LIBRARIAN;
        this.lib = new LibrarianUser();          
        this.reader = new NonReaderUser();
    }

    public void setReaderRole() {
        this.role = Role.READER;
        this.reader = new ReaderUser(id, userName, phoneNumber, borrowStorage);
        this.lib = new NonLibrarianUser();
    }


    private Object readResolve() {
        if (this.role == Role.LIBRARIAN) {
            this.lib = new LibrarianUser();
            this.reader = new NonReaderUser();
        } else {
            this.reader = new ReaderUser(id, userName, phoneNumber, borrowStorage);
            this.lib = new NonLibrarianUser();
        }
        return this;
    }
}
