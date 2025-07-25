public class TestUser {
    public static void main(String[] args){
        BorrowStorage bos = new BorrowStorage(new BookStorage());
        User me = new User(1000, "me", "123", bos);
        me.setLibrarianRole();
        me.showAllBooks();
        User reader = new User(10001, "Phuoc", "12321312", bos);
        reader.setReaderRole();
        reader.borrowBook();
        me.showAllBooks();
    }
}
