import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;
public class BookStorage {
    private ArrayList<Book> books;
    public BookStorage(){
        File file = new File("./database/books.obj");
        if (file.exists() && file.length() != 0){
            try{
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                books = (ArrayList<Book>) ois.readObject();  
                ois.close();
            } catch (Exception ex){
                books = new ArrayList<Book>();
                ex.printStackTrace();
            } 
        } else{
            books = new ArrayList<>();
        }
    }
    public ArrayList<Book> getBooks(){return books;}
    public void addBook(Book book){
        books.add(book);
        saveBook();
    }
    public void addBooks(ArrayList<Book> manyBooks){
        for (Book book : manyBooks){
            books.add(book);
        }
        saveBook();
    }
    public void saveBook(){
        try{
            File file = new File("./database/books.obj");
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(books);
            oos.close();
        } catch(Exception exception){
            exception.printStackTrace();
        }

    }
    public void showBookbyBook(){
        for (Book book : books){
            System.out.println(book.toString());
            System.out.println("-------------");
        }
    }
    public void showBookById(int id){
        int quant = 0;
        for (Book book : books){
            if (book.getId() == id){
                quant++;
            }
        }
        System.out.println("Book with id " + id + "has " + quant + " books.");
    }
    public void showAllBooks() {
        // Use a map to store books by ID and count them
        HashMap<Integer, Integer> bookCountMap = new HashMap<>();
        HashMap<Integer, Book> bookReferenceMap = new HashMap<>();

        for (Book book : books) {
            int id = book.getId();
            bookCountMap.put(id, bookCountMap.getOrDefault(id, 0) + 1);
            bookReferenceMap.putIfAbsent(id, book); // Keep one instance for display
        }

        for (Integer id : bookCountMap.keySet()) {
            Book book = bookReferenceMap.get(id);
            int quantity = bookCountMap.get(id);
            System.out.println(book.toString());
            System.out.println("Quantity: " + quantity);
            System.out.println("-------------");
        }
}

    public void removeBookById(int id){
        for (Book book : books){
            if (book.getId() == id){
                books.remove(book);
                return;
            }
        }
    }
}
