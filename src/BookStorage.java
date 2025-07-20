import java.util.ArrayList;
import java.io.*;
public class BookStorage {
    private ArrayList<Book> books;
    public BookStorage() throws IOException, ClassNotFoundException{
        File file = new File("./database/books.obj");
        if (file.exists()){
            try (FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis)){
                books = (ArrayList<Book>) ois.readObject();    
            }
        } else{
            books = new ArrayList();
        }
    }
    public ArrayList<Book> getBooks(){return books;}
    public void addBook(Book book){
        books.add(book);
    }
    public Book getBook(int id){
        for (Book book : books){
            if (book.getId() == id){
                return book;
            }
        }
        return new Book(0, "1", "1", 0);
    }
}
