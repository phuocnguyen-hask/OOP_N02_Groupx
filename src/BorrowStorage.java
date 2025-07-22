import java.util.*;
import java.io.*;

public class BorrowStorage {
    private ArrayList<Book> borrowBooks;

    public BorrowStorage() {
        reloadBorrowedBooks();
    }

    private void reloadBorrowedBooks() {
        File file = new File("database/borrowedBooks.obj");
        if (file.exists() && file.length() != 0) {
            try {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                borrowBooks = (ArrayList<Book>) ois.readObject();
                ois.close();
            } catch (Exception e) {
                e.printStackTrace();
                borrowBooks = new ArrayList<>();
            }
        } else {
            borrowBooks = new ArrayList<>();
        }
    }

    public void saveBorrowedBook() {
        File file = new File("database/borrowedBooks.obj");
        try {
            file.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(borrowBooks);
            oos.close();
            System.out.println("Borrowed books saved successfully to: " + file.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Failed to save borrowed books.");
        }
    }

    public void addBorrowedBooksByID(int id, int quant) {
        reloadBorrowedBooks();
        BookStorage bookStorage = new BookStorage();
        ArrayList<Book> matchedBooks = new ArrayList<>();

        for (Book book : bookStorage.getBooks()) {
            if (book.getId() == id) {
                matchedBooks.add(book);
                if (matchedBooks.size() == quant) break;
            }
        }

        if (matchedBooks.size() < quant) {
            System.out.println("Only " + matchedBooks.size() + " book(s) available with ID: " + id + ". Unable to borrow " + quant + ".");
            return;
        }

        for (Book book : matchedBooks) {
            borrowBooks.add(book);
            bookStorage.removeBook(book);
        }

        saveBorrowedBook();
        System.out.println("Successfully borrowed " + quant + " book(s) with ID: " + id);
    }

    public void addBorrowedBooks(Book book, int quant) {
        reloadBorrowedBooks();
        BookStorage bookStorage = new BookStorage();

        for (int i = 0; i < quant; ++i) {
            borrowBooks.add(book);
            bookStorage.removeBook(book);
        }

        saveBorrowedBook();
        System.out.println("Successfully borrowed " + quant + " book(s).");
    }

    public void deleteBorrowedBooks(Book book, int quant) {
        reloadBorrowedBooks();
        BookStorage bookStorage = new BookStorage();
        int removed = 0;
        Iterator<Book> it = borrowBooks.iterator();

        while (it.hasNext() && removed < quant) {
            Book b = it.next();
            if (b.getId() == book.getId()) {
                it.remove();
                bookStorage.addBook(book);
                removed++;
            }
        }

        saveBorrowedBook();
        System.out.println("Returned " + removed + " book(s).");
    }

    public void showAllBorrowedBook() {
        reloadBorrowedBooks();

        HashMap<Integer, Integer> bookCountMap = new HashMap<>();
        HashMap<Integer, Book> bookReferenceMap = new HashMap<>();

        for (Book book : borrowBooks) {
            int id = book.getId();
            bookCountMap.put(id, bookCountMap.getOrDefault(id, 0) + 1);
            bookReferenceMap.putIfAbsent(id, book);
        }

        if (bookCountMap.isEmpty()) {
            System.out.println("No borrowed books found.");
        } else {
            for (Integer id : bookCountMap.keySet()) {
                Book book = bookReferenceMap.get(id);
                int quantity = bookCountMap.get(id);
                System.out.println(book.toString());
                System.out.println("Quantity: " + quantity);
                System.out.println("-------------");
            }
        }
    }
}
