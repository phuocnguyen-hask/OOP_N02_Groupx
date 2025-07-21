import java.util.ArrayList;
import java.io.*;
public class BookStorage {
    private ArrayList<Book> books;
    public BookStorage() throws IOException, ClassNotFoundException{
        File file = new File("./database/books.obj");
        if (file.exists() && file.length() != 0){
            try (FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis)){
                books = (ArrayList<Book>) ois.readObject();    
            }
        } else{
            books = new ArrayList<>();
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
        return null;
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
    public int getQuant(){
        int quant = 0;
        for (Book book : books){
            quant += book.getQuantity();
        }
        return quant;
    }
    public void removeDuplicatedBook() {
        ArrayList<Book> uniqueBooks = new ArrayList<>();

        for (Book book : books) {
            boolean alreadyExists = false;

            for (Book uniqueBook : uniqueBooks) {
                if (book.getId() == uniqueBook.getId()) {
                    alreadyExists = true;
                    break;
                }
            }

            if (!alreadyExists) {
                uniqueBooks.add(book);
            }
        }

        books = uniqueBooks;
    }
    public void showBooks(){
        for (Book book : books){
            System.out.println(book.toString());
            System.out.println("-------------");
        }
    }
}
