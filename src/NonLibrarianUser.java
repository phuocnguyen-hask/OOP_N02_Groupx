import java.io.Serializable;

public class NonLibrarianUser implements Librarian, Serializable{
    private static final long serialVersionUID = 1L;

    public void addBook(Book book){
        System.out.println("Can't add book");
    }
    public BookStorage getStorage(){
        return null;
    }
    public void removeBook(int BookId){
        //can't remove
        System.out.println("Can't remove book");
    }
    public void manageUser(){
        //don't have permission
        System.out.println("Don't have permisson");
    }
    public void showAllBooks(){
        //don't have permission
        System.out.println("Don't have permission");
    }
    
}
