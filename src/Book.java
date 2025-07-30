import java.io.Serializable;

public class Book implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String title;
    private String author;
    private int quantity; // NEW

    public Book(int id, String title, String author){
        this.id = id;
        this.title = title;
        this.author = author;
        this.quantity = 1; // mặc định 1 bản khi tạo mới
    }

    public int getId(){ return id; }
    public String getTitle(){ return title; }
    public String getAuthor(){ return author; }
    public int getQuantity(){ return quantity; }       // NEW

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setQuantity(int quantity) { this.quantity = Math.max(0, quantity); } // NEW

    @Override
    public String toString(){
        return "Id: " + id +
               "\nTitle: " + title +
               "\nAuthor: " + author +
               "\nQuantity: " + quantity;
    }
}
