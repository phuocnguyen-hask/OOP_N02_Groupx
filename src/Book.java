import java.io.Serializable;

public class Book implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String title;
    private String author;
    private int quantity; 

    public Book(int id, String title, String author){
        this.id = id;
        this.title = title;
        this.author = author;
        this.quantity = 1; 
    }

    public int getId(){ return id; }
    public String getTitle(){ return title; }
    public String getAuthor(){ return author; }
    public int getQuantity(){ return quantity; }    

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setQuantity(int quantity) { this.quantity = Math.max(0, quantity); } 

    @Override
    public String toString(){
        return "Id: " + id +
               "\nTitle: " + title +
               "\nAuthor: " + author +
               "\nQuantity: " + quantity;
    }
}
