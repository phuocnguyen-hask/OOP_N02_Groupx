public class Main {
    public static void main(String[] args) {
        UserDatabase userDatabase = new UserDatabase();
        userDatabase.showAllUsers();

        new LoginFrame(userDatabase);
    }
}
