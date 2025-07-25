public class TestUser {
    public static void main(String[] args){
        User me = new User("me", "123");
        me.setLibrarianRole();
        me.removeBook(101);
        me.showAllBooks();
    }
}
