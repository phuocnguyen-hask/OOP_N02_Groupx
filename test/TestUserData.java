public class TestUserData {
    public static void main(String[] args){
        BookStorage bs = new BookStorage();
        BorrowStorage bos = new BorrowStorage(bs);
        UserDatabase udb = new UserDatabase();
        User user1 = new User(1, "phuoc2", "123", bos);
        user1.setLibrarianRole();
        udb.addUser(user1);
        udb.showAllUsers();
    }
}
