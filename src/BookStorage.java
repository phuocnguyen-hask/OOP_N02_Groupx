import java.io.*;
import java.util.*;

public class BookStorage implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<Book> books;

    // Chỉ mục theo ID để tra cứu O(1) – không serialize
    private transient Map<Integer, Book> indexById;

    public BookStorage() {
        loadBooksFromFile();  // Load once during initialization
        rebuildIndex();
    }

    // ----------- Public Methods -----------

    public ArrayList<Book> getBooks() {
        return books;
    }

    public void addBook(Book book) {
        if (book == null) return;
        if (indexById == null) rebuildIndex();

        Book existing = indexById.get(book.getId());
        if (existing == null) {
            // ID chưa có -> thêm mới
            books.add(book);
            indexById.put(book.getId(), book);
            saveBooksToFile();
            return;
        }

        // ID đã có -> so Title/Author
        if (equalsIgnoreCase(existing.getTitle(), book.getTitle())
                && equalsIgnoreCase(existing.getAuthor(), book.getAuthor())) {
            // gộp: +1 bản
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

    // Giảm 1 bản (hoặc xoá đầu sách nếu còn 1)
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

    // Giảm 1 bản theo đối tượng (giữ tên cũ)
    public void removeBook(Book book) {
        if (book == null) return;
        removeBookById(book.getId());
    }

    public Book findBookById(int id) {
        if (indexById == null) rebuildIndex();
        return indexById.get(id);
    }

    // ---- HÀM MỚI: xoá N bản hoặc xoá hết ----

    /** Giảm 'copies' bản cho ID; nếu copies >= quantity hiện tại thì xoá đầu sách. */
    public boolean removeCopiesById(int id, int copies) {
        if (copies <= 0) return false;
        if (indexById == null) rebuildIndex();

        Book b = indexById.get(id);
        if (b == null) return false;

        if (b.getQuantity() > copies) {
            b.setQuantity(b.getQuantity() - copies);
        } else {
            books.remove(b);
            indexById.remove(id);
        }
        saveBooksToFile();
        return true;
    }

    /** Xoá toàn bộ bản của ID. */
    public boolean removeAllCopiesById(int id) {
        if (indexById == null) rebuildIndex();
        Book b = indexById.get(id);
        if (b == null) return false;
        books.remove(b);
        indexById.remove(id);
        saveBooksToFile();
        return true;
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

    // ----------- Private helpers -----------

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

    // ----------- Private I/O Methods -----------

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
