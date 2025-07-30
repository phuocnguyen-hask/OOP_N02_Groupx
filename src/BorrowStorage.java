import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class BorrowStorage implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<BorrowBook> borrowList;
    private final BookStorage bookStorage;  // Access to central book storage

    public BorrowStorage(BookStorage bookStorage) {
        this.bookStorage = bookStorage;
        loadBorrowListFromFile();
    }

    // ----------------- Public API -----------------

    public void borrowBook(int readerId, int bookId, LocalDate borrowDate, LocalDate returnDate) {
        // Tìm đầu sách theo ID trong kho
        Book fromStore = bookStorage.findBookById(bookId);
        if (fromStore == null) {
            System.out.println("Book with ID " + bookId + " not found.");
            return;
        }

        // Tạo snapshot Book đơn giản để lưu vào BorrowBook (tránh mang theo quantity)
        Book borrowedCopy = new Book(fromStore.getId(), fromStore.getTitle(), fromStore.getAuthor());

        // Tạo bản ghi mượn
        BorrowBook borrow = new BorrowBook(readerId, borrowedCopy, borrowDate, returnDate);
        borrowList.add(borrow);

        // Giảm 1 bản trong kho (đúng quy tắc quantity)
        bookStorage.removeBookById(bookId);

        saveBorrowListToFile();
        System.out.println("Book borrowed: " + borrowedCopy.getTitle());
    }

    public void returnBook(int readerId, int bookId) {
        Iterator<BorrowBook> iterator = borrowList.iterator();
        while (iterator.hasNext()) {
            BorrowBook borrow = iterator.next();
            if (borrow.getReaderId() == readerId && borrow.getBook().getId() == bookId) {
                // Trả 1 bản về kho (BookStorage sẽ gộp nếu trùng ID/Title/Author)
                Book b = borrow.getBook();
                bookStorage.addBook(new Book(b.getId(), b.getTitle(), b.getAuthor()));

                iterator.remove();
                saveBorrowListToFile();
                System.out.println("Book returned: " + b.getTitle());
                return;
            }
        }
        System.out.println("No matching borrowed book found.");
    }

    public void showAllBorrowedBooks() {
        if (borrowList.isEmpty()) {
            System.out.println("No books currently borrowed.");
            return;
        }
        for (BorrowBook borrow : borrowList) {
            System.out.println(borrow);
            System.out.println("---------------");
        }
    }

    public void showBorrowedByReader(int readerId) {
        boolean found = false;
        for (BorrowBook borrow : borrowList) {
            if (borrow.getReaderId() == readerId) {
                System.out.println(borrow);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No borrowed books for reader: " + readerId);
        }
    }

    // ----------------- I/O helpers -----------------

    private File getBorrowFile() {
        return new File("database/borrow.obj");
    }

    private void saveBorrowListToFile() {
        File file = getBorrowFile();
        file.getParentFile().mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(borrowList);
            System.out.println("Borrow list saved.");
        } catch (Exception e) {
            System.err.println("Failed to save borrow list.");
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadBorrowListFromFile() {
        File file = getBorrowFile();
        if (file.exists() && file.length() != 0) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                borrowList = (ArrayList<BorrowBook>) ois.readObject();
                System.out.println("Borrow list loaded.");
            } catch (java.io.InvalidClassException ice) {
                // KHẮC PHỤC: file cũ không tương thích serialVersionUID → backup & reset
                System.err.println("Borrow file version mismatch. Backing up and starting fresh.");
                try {
                    File bak = new File(file.getParent(), file.getName() + ".bak");
                    if (bak.exists()) bak.delete();
                    if (!file.renameTo(bak)) {
                        System.err.println("Could not backup old borrow file.");
                    }
                } catch (Exception ignore) {}
                borrowList = new ArrayList<>();
            } catch (Exception e) {
                borrowList = new ArrayList<>();
                System.err.println("Failed to load borrow list. Starting fresh.");
                e.printStackTrace();
            }
        } else {
            borrowList = new ArrayList<>();
            System.out.println("No borrow list found. Starting fresh.");
        }
    }
}
