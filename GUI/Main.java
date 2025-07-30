import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UserDatabase userDatabase = new UserDatabase();
            BookStorage bookStorage = new BookStorage();          // kho sách dùng chung
            BorrowStorage borrowStorage = new BorrowStorage(bookStorage); // mượn trả dùng chung

            // Mở màn hình đăng nhập, truyền DB + borrowStorage dùng chung
            new LoginFrame(userDatabase, borrowStorage);
        });
    }
}
