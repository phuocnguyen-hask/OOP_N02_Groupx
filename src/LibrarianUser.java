import java.io.Serializable;

public class LibrarianUser implements Librarian, Serializable{
    BookStorage bookStorage;
    public LibrarianUser(){
        this.bookStorage = new BookStorage();
    }
    public BookStorage getStorage(){
        return this.bookStorage;
    }

    public void addBook(Book book){
        this.bookStorage.addBook(book);
        System.out.println("add successfully");
    }
    public void removeBook(int id){
        this.bookStorage.removeBookById(id);
        System.out.println("remove successfully");
    }
    public void showAllBooks(){
        bookStorage.showAllBooks();
    }
    public void manageUser(){

    }
}
