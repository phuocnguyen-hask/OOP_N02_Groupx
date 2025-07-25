public class Main {
    public static void main(String[] args) {
        BookStorage bos = new BookStorage();
        BorrowStorage boo = new BorrowStorage(bos);
        UserDatabase userDatabase = new UserDatabase();
        userDatabase.showAllUsers();
        User user1 = new User(10, "Linh1223", "123456", boo);
        user1.setLibrarianRole();
        userDatabase.addUser(user1);
        userDatabase.showAllUsers();

        new LoginFrame(userDatabase);
    }
}
