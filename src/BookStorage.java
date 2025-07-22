import java.io.*;
import java.util.*;

public class BookStorage {
    private ArrayList<Book> books;

    public BookStorage() {
        loadBooksFromFile();  // Load once during initialization
    }

    // ----------- Public Methods -----------

    public ArrayList<Book> getBooks() {
        return books;
    }

    public void addBook(Book book) {
        books.add(book);
        saveBooksToFile();
    }

    public void addBooks(ArrayList<Book> manyBooks) {
        books.addAll(manyBooks);
        saveBooksToFile();
    }

    public void removeBookById(int id) {
        Iterator<Book> iterator = books.iterator();
        while (iterator.hasNext()) {
            Book book = iterator.next();
            if (book.getId() == id) {
                iterator.remove();
                saveBooksToFile();
                return; // Remove only one copy
            }
        }
    }

    public void removeBook(Book book) {
        books.remove(book);
        saveBooksToFile();
    }

    public void showBookById(int id) {
        int quantity = 0;
        for (Book book : books) {
            if (book.getId() == id) {
                quantity++;
            }
        }
        System.out.println("Book with ID " + id + " has " + quantity + " copies.");
    }

    public void showAllBooks() {
        Map<Integer, Integer> countMap = new HashMap<>();
        Map<Integer, Book> referenceMap = new HashMap<>();

        for (Book book : books) {
            int id = book.getId();
            countMap.put(id, countMap.getOrDefault(id, 0) + 1);
            referenceMap.putIfAbsent(id, book);  // Keep one for display
        }

        for (Integer id : countMap.keySet()) {
            Book book = referenceMap.get(id);
            int quantity = countMap.get(id);
            System.out.println(book);
            System.out.println("Quantity: " + quantity);
            System.out.println("-------------");
        }
    }

    public void showBookByBook() {
        for (Book book : books) {
            System.out.println(book);
            System.out.println("-------------");
        }
    }

    public void reloadBooksFromFile() {
        loadBooksFromFile();
    }

    // ----------- Private I/O Methods -----------

    private void loadBooksFromFile() {
        File file = new File("database/books.obj");
        if (file.exists() && file.length() != 0) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                books = (ArrayList<Book>) ois.readObject();
                System.out.println("Books loaded from: " + file.getAbsolutePath());
            } catch (Exception ex) {
                books = new ArrayList<>();
                System.err.println("Failed to load books. Starting with an empty list.");
                ex.printStackTrace();
            }
        } else {
            books = new ArrayList<>();
            System.out.println("No existing book data. Starting fresh.");
        }
    }

    private void saveBooksToFile() {
        File file = new File("database/books.obj");
        file.getParentFile().mkdirs();  // Ensure folder exists

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(books);
            System.out.println("Books saved to: " + file.getAbsolutePath());
        } catch (Exception ex) {
            System.err.println("Error saving books.");
            ex.printStackTrace();
        }
    }
}
