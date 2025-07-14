import java.io.Serializable;

class Book implements Serializable{
    private String bookID;
    private String title;
    private String author;
    private String category;
    private int quant;
    private String status;
    private double price;
    public Book(String bookID, String title, String author, String category, int quant, String status, double price){
        this.bookID = bookID;
        this.title = title;
        this.author = author;
        this.category = category;
        this.quant = quant;
        this.status = status;
        this.price = price;
    }
    public void addBook(){
        System.out.println("Book added");
    }
    public void editBook(){
        System.out.println("Book's edited");
    }
    public void deleteBook(){
        System.out.println("Bok's deleted");
    }
    public String getBookID(){
        return bookID;
    };
    public String getTitle(){
        return title;
    };
    public String getAuthor(){
        return author;
    }
    public String getCategory(){
        return category;
    }
    public int getQuant(){
        return quant;
    }
    public String getStatus(){
        return status;
    };
    public double getPrice(){
        return price;
    }
    public void printBook(){
        System.out.println("BookID: " + this.getBookID());
        System.out.println("Title: " + this.getTitle());
        System.out.println("Author: " + this.getAuthor());
        System.out.println("Category: " + this.getCategory());
        System.out.println("Quantity: " + this.getQuant());
        System.out.println("Status: " + this.getStatus());
        System.out.println("Price: " + this.getPrice());
    }
}