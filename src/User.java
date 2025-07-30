import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    // KHÔNG serialize trực tiếp object hành vi (dễ lỗi version khi thay đổi code)
    private transient Reader reader;
    private transient Librarian lib;

    private int id;
    private String userName;
    private String passWord;

    // Lưu vai trò để khôi phục sau khi deserialize
    public enum Role { READER, LIBRARIAN }
    private Role role = Role.READER;   // mặc định: READER

    // Dùng chung cho mượn/trả -> nắm luôn BookStorage
    private BorrowStorage borrowStorage;

    public User(int id, String userName, String passWord, BorrowStorage borrowStorage) {
        this.id = id;
        this.userName = userName;
        this.passWord = passWord;
        this.borrowStorage = borrowStorage;

        // Khởi tạo mặc định (chưa set role cụ thể)
        this.reader = new NonReaderUser();
        this.lib = new NonLibrarianUser();
    }

    // ====== Getters cơ bản ======
    public String username() { return this.userName; }
    public String passWord() { return this.passWord; }
    public int getId()       { return this.id; }
    public Librarian getLib(){ return this.lib; }
    public Reader getReader(){ return this.reader; }

    // ====== Role helpers ======
    public boolean isLibrarian() { return role == Role.LIBRARIAN; }
    public Role getRole() { return role; }

    // ====== BorrowStorage (để “bơm” lại nếu user cũ thiếu) ======
    public BorrowStorage getBorrowStorage() { return this.borrowStorage; }
    public void setBorrowStorage(BorrowStorage borrowStorage) { this.borrowStorage = borrowStorage; }

    /** Lấy BookStorage an toàn: ưu tiên qua BorrowStorage (ổn định cho cả Reader/Librarian). */
    public BookStorage getStorage() {
        if (this.borrowStorage != null) return this.borrowStorage.getBookStorage();
        if (this.lib != null) return this.lib.getStorage();
        return null;
    }

    // ====== Librarian functions (giữ nguyên tên API cũ) ======
    public void addBook(Book book) { this.lib.addBook(book); }
    public void removeBook(int bookId) { this.lib.removeBook(bookId); }
    public void showAllBooks() { this.lib.showAllBooks(); }

    public void manageUser() {
        // To implement
    }

    // ====== Reader functions (giữ nguyên tên API cũ) ======
    public void borrowBook() { this.reader.borrowBook(); }
    public void returnBook() { this.reader.returnBook(); }

    // ====== Đổi vai trò (cập nhật role + instance hành vi) ======
    public void setLibrarianRole() {
        this.role = Role.LIBRARIAN;
        this.lib = new LibrarianUser();          // nếu có ctor nhận BookStorage thì truyền getStorage()
        this.reader = new NonReaderUser();
    }

    public void setReaderRole() {
        this.role = Role.READER;
        this.reader = new ReaderUser(id, userName, borrowStorage);
        this.lib = new NonLibrarianUser();
    }

    /** Sau khi deserialize: khôi phục reader/lib đúng theo role đã lưu. */
    private Object readResolve() {
        if (this.role == Role.LIBRARIAN) {
            this.lib = new LibrarianUser();
            this.reader = new NonReaderUser();
        } else {
            this.reader = new ReaderUser(id, userName, borrowStorage);
            this.lib = new NonLibrarianUser();
        }
        return this;
    }
}
