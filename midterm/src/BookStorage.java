package src;
import java.io.*;
import java.util.ArrayList;
public class BookStorage {
    private ArrayList<Book> books;
    public static void main(String[] args){
        
    }
    public BookStorage(){
        try{
            File file = new File("../database/bookstorage.ser");
            if (file.length() != 0){
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                books = (ArrayList<Book>) ois.readObject();
                ois.close();
            }
            books = new ArrayList<Book>();
        } catch (Exception e){
            books = new ArrayList<Book>();
            e.printStackTrace();
        }
    }
    public void saveBooks(){
        try{
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(new File("./database/bookstorage.ser")));
            os.writeObject(books);
            os.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public ArrayList<Book> getBooks(){
        return books;
    }
    public void removeBook(String id){
        for (Book book : books){
            if (book.getBookID().equals(id)){
                books.remove(book);
                saveBooks();
                break;
            }
        }
    }
    public void editBook(String id, Book newBook){
        boolean found = false;
        for (int i = 0; i < books.size(); ++i){
            if (books.get(i).getBookID().equals(id)){
                books.set(i, newBook);
                found = true;
            }
        }
        if (found){
            saveBooks();
            System.out.println("Change book with ID " + id + "Successfully");
        }
        else{
            System.out.println("Can't find book");
        }
    }
    public void showAllBooks(){
        if (books.size() == 0){System.out.println("No book");}
        for (Book book : books){
            System.out.println(book.toString());
            System.out.println("---------------");
        }
    }
    
}
