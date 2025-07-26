import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class BorrowStorage implements Serializable{
    private static final long serialVersionUID = 1L;

    private ArrayList<BorrowBook> borrowList;
    private BookStorage bookStorage;  // Access to central book storage

    public BorrowStorage(BookStorage bookStorage) {
        this.bookStorage = bookStorage;
        loadBorrowListFromFile();
    }

    public void borrowBook(int readerId, int bookId, LocalDate borrowDate, LocalDate returnDate) {
        Book bookToBorrow = null;
        for (Book book : bookStorage.getBooks()) {
            if (book.getId() == bookId) {
                bookToBorrow = book;
                break;
            }
        }

        if (bookToBorrow == null) {
            System.out.println("Book with ID " + bookId + " not found.");
            return;
        }

        // Proceed with borrowing
        BorrowBook borrow = new BorrowBook(readerId, bookToBorrow, borrowDate, returnDate);
        borrowList.add(borrow);
        bookStorage.removeBook(bookToBorrow);  // Remove 1 copy from storage
        saveBorrowListToFile();
        System.out.println("Book borrowed: " + bookToBorrow.getTitle());
    }

    public void returnBook(int readerId, int bookId) {
        Iterator<BorrowBook> iterator = borrowList.iterator();
        while (iterator.hasNext()) {
            BorrowBook borrow = iterator.next();
            if (borrow.getReaderId() == readerId && borrow.getBook().getId() == bookId) {
                bookStorage.addBook(borrow.getBook());  // Return 1 copy to storage
                iterator.remove();
                saveBorrowListToFile();
                System.out.println("Book returned: " + borrow.getBook().getTitle());
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

    private void saveBorrowListToFile() {
        File file = new File("database/borrow.obj");
        file.getParentFile().mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(borrowList);
            System.out.println("Borrow list saved.");
        } catch (Exception e) {
            System.err.println("Failed to save borrow list.");
            e.printStackTrace();
        }
    }

    private void loadBorrowListFromFile() {
        File file = new File("database/borrow.obj");
        if (file.exists() && file.length() != 0) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                borrowList = (ArrayList<BorrowBook>) ois.readObject();
                System.out.println("Borrow list loaded.");
            } catch (Exception e) {
                borrowList = new ArrayList<>();
                System.err.println("Failed to load borrow list.");
                e.printStackTrace();
            }
        } else {
            borrowList = new ArrayList<>();
            System.out.println("No borrow list found. Starting fresh.");
        }
    }
}
