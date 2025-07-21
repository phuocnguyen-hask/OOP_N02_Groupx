package src;
import java.io.Serializable;

public class Book implements Serializable{
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
    public String toString(){
        return "BookID: " + this.getBookID() +
        "Title: " + this.getTitle() +
        "Author: " + this.getAuthor() +
        "Category: " + this.getCategory() +
        "Quantity: " + this.getQuant() +
        "Status: " + this.getStatus() +
        "Price: " + this.getPrice();
    }
}