import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        BookStorage bos = new BookStorage();
        BorrowStorage bs = new BorrowStorage(bos); // if this has a no-arg constructor

        ArrayList<User> users = new ArrayList<>();
        User reader = new User(1, "reader1", "123", bs);
        User librarian = new User(2, "lib1", "456", bs);

        users.add(reader);
        users.add(librarian);

        new LoginFrame(users);
    }
}

