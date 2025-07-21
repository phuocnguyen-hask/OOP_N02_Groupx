package src;

public class Book1 {
    private String id;
    private String title;
    private String author;
    private boolean available;

    public Book1(String id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.available = true;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isAvailable() { return available; }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return String.format("%s - %s - %s - %s",
                id, title, author, (available ? "Có sẵn" : "Đã mượn"));
    }
}

