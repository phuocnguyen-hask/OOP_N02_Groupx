import java.io.*;
import java.util.*;

public class BookStorage implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<Book> books;

    private transient Map<Integer, Book> indexById;

    public BookStorage() {
        loadBooksFromFile(); 
        rebuildIndex();
    }

    // ----------- pt public -----------

    public ArrayList<Book> getBooks() {
        return books;
    }

    public void addBook(Book book) {
        if (book == null) return;
        if (indexById == null) rebuildIndex();

        Book existing = indexById.get(book.getId());
        if (existing == null) {
            books.add(book);
            indexById.put(book.getId(), book);
            saveBooksToFile();
            return;
        }
        if (equalsIgnoreCase(existing.getTitle(), book.getTitle())
                && equalsIgnoreCase(existing.getAuthor(), book.getAuthor())) {
            existing.setQuantity(existing.getQuantity() + 1);
            saveBooksToFile();
        } else {
            System.err.println("[BookStorage] Add blocked: same ID " + book.getId()
                    + " but different Title/Author.");
        }
    }

    public void addBooks(ArrayList<Book> manyBooks) {
        if (manyBooks == null) return;
        for (Book b : manyBooks) addBook(b); // addBook đã tự save
    }

    public void removeBookById(int id) {
        if (indexById == null) rebuildIndex();
        Book existing = indexById.get(id);
        if (existing == null) return;

        if (existing.getQuantity() > 1) {
            existing.setQuantity(existing.getQuantity() - 1);
        } else {
            books.remove(existing);
            indexById.remove(id);
        }
        saveBooksToFile();
    }

    public void removeBook(Book book) {
        if (book == null) return;
        removeBookById(book.getId());
    }

    public Book findBookById(int id) {
        if (indexById == null) rebuildIndex();
        return indexById.get(id);
    }


    public boolean removeCopiesById(int id, int copies) {
        if (copies <= 0) return false;
        boolean changed = false;
        for (int i = 0; i < copies; i++) {
            Book b = findBookById(id);
            if (b == null) break;     // hết sách để xoá
            removeBookById(id);
            changed = true;
        }
        return changed;
    }

    public boolean removeAllCopiesById(int id) {
        boolean changed = false;
        while (findBookById(id) != null) {
            removeBookById(id);      
            changed = true;
        }
        return changed;
    }


    public void showBookById(int id) {
        Book existing = findBookById(id);
        if (existing == null) {
            System.out.println("No book with ID " + id + ".");
        } else {
            System.out.println(existing);
            System.out.println("Quantity: " + existing.getQuantity());
        }
    }

    public void showAllBooks() {
        for (Book book : books) {
            System.out.println(book);
            System.out.println("Quantity: " + book.getQuantity());
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
        rebuildIndex();
    }


    private boolean equalsIgnoreCase(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equalsIgnoreCase(b);
    }

    private void rebuildIndex() {
        indexById = new HashMap<>();
        if (books != null) {
            for (Book b : books) indexById.put(b.getId(), b);
        }
    }

    // ----------- pt private-----------

    @SuppressWarnings("unchecked")
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
        file.getParentFile().mkdirs();

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(books);
            System.out.println("Books saved to: " + file.getAbsolutePath());
        } catch (Exception ex) {
            System.err.println("Error saving books.");
            ex.printStackTrace();
        }
    }
}
