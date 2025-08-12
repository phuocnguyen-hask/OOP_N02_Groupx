import java.io.Serializable;

public class NonReaderUser implements Reader, Serializable{
    private static final long serialVersionUID = 1L;

    public void borrowBook(){
        System.out.println("can't borrow");
    }
    public void returnBook(){
        //can't return book
        System.out.println("Can't return");
    }
    public String getPhoneNumber() {
        return "hello, world";
    }

}