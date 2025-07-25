import java.io.Serializable;

public class NonReaderUser implements Reader, Serializable{
    public void borrowBook(){
        System.out.println("can't borrow");
    }
    public void returnBook(){
        //can't return book
        System.out.println("Can't return");
    }
}