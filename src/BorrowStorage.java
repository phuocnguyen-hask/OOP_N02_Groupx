import java.util.*;
import java.io.*;
public class BorrowStorage {
    private ArrayList<Book> borrowBooks;
    public BorrowStorage(){
        File file = new File("database/borrowedBooks.obj");
        if (file.exists() && file.length() != 0){
            try{
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                borrowBooks = (ArrayList<Book>) ois.readObject();
                ois.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        } else{
            borrowBooks = new ArrayList<Book>();
        }
    }
    public void saveBorrowedBook() {
        File file = new File("database/borrowedBooks.obj"); // updated path
        try {
            // Ensure the parent directory exists
            file.getParentFile().mkdirs();  // creates 'database' inside 'Web' if needed

            // Save the borrowedBooks list
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
        BookStorage bookStorage = new BookStorage();
        ArrayList<Book> matchedBooks = new ArrayList<>();

        // First collect the books matching the ID
        for (Book book : bookStorage.getBooks()) {
            if (book.getId() == id) {
                matchedBooks.add(book);
                if (matchedBooks.size() == quant) break;
            }
        }

        // Check if enough books are found
        if (matchedBooks.size() < quant) {
            System.out.println("Only " + matchedBooks.size() + " book(s) available with ID: " + id + ". Unable to borrow " + quant + ".");
            return;
        }

        // Add to borrowed list and remove from storage
        for (Book book : matchedBooks) {
            borrowBooks.add(book);
            bookStorage.removeBook(book);
        }

        saveBorrowedBook();
        System.out.println("Successfully borrowed " + quant + " book(s) with ID: " + id);
    }

    public void addBorrowedBooks(Book book, int quant){
        //add books to borrowBooks and remove from bookStorage
        BookStorage bookStorage = new BookStorage();

        for (int i = 0; i < quant; ++i){
            borrowBooks.add(book);
            saveBorrowedBook();
            bookStorage.removeBook(book);
        }
    }
    public void deleteBorrowedBooks(Book book, int quant){
        BookStorage bookStorage = new BookStorage();
        for (int i = 0; i < quant; ++i){
            //reverse of add
            borrowBooks.remove(book);
            saveBorrowedBook();
            bookStorage.addBook(book);
        }
    }
    public void showAllBorrowedBook() {
        // Map to count each borrowed book by ID
        HashMap<Integer, Integer> bookCountMap = new HashMap<>();
        // Map to store one reference of each Book for display
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
                System.out.println(book.toString()); // Ensure Book has a good toString() method
                System.out.println("Quantity: " + quantity);
                System.out.println("-------------");
            }
        }
    }

    
}
