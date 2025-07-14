import java.io.*;
import java.util.ArrayList;
public abstract class BookStorage {
    public static void main(String[] args){
        Book oneBook = new Book("2893", "MeowMeow", "Me", "Fantasy", 23, "available", 23000);
        ArrayList<Book> arrayList = new ArrayList<>();
        arrayList.add(oneBook);
        BookStorage.saveBooks(arrayList);
        ArrayList<Book> bookStorage = BookStorage.getBooks();
        for (Book book : bookStorage){
            book.printBook();
        }
        BookStorage.editBook("2893", new Book("2800", "Gaugau", "Me", "Dark Fantasy", 23, "available", 23000));
        bookStorage = BookStorage.getBooks();
        for (Book book : bookStorage){
            book.printBook();
        }
    }   
    
    private static final String books = "bookstorage.ser";
    public static void saveBooks(ArrayList<Book> bookList){
        try{
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(books));
            os.writeObject(bookList);
            os.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public static ArrayList<Book> getBooks(){
        File file = new File(books);
        if (! file.exists()){
            return new ArrayList<>();
        }
        try {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(books));
            ArrayList<Book> bStorage = (ArrayList<Book>) is.readObject();
            is.close();
            return bStorage;
        } catch(Exception e){
            return new ArrayList<>();
        }
    }
    public static void removeBook(String id){
        ArrayList<Book> books = BookStorage.getBooks();
        boolean remove = books.removeIf(book -> book.getBookID().equals(id));
        if (remove){
            BookStorage.saveBooks(books);
            System.out.println("Remove book with ID " + id + " successfully");
        }else{
            System.out.println("Can't remove that book with ID: " + id);
        }
    }
    public static void editBook(String id, Book newBook){
        ArrayList<Book> books = BookStorage.getBooks();
        boolean found = false;
        for (int i = 0; i < books.size(); ++i){
            if (books.get(i).getBookID().equals(id)){
                books.set(i, newBook);
                found = true;
            }
        }
        if (found){
            BookStorage.saveBooks(books);
            System.out.println("Change book with ID " + id + "Successfully");
        }
        else{
            System.out.println("Can't find book");
        }
    }
    
}
