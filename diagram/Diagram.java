
public class Diagram {
    public static void main(String[] args){
        Librarian lib = new Librarian("admin", "1234", "Thu Thu", "092003");
        if (lib.login("admin", "1234")) {
            System.out.println("Login successful!");
            lib.qliSach();
            lib.qliNguoiDoc();
        }

        Book book = new Book("2910Ha2", "Lập trình Java", "Nguyễn Văn A", "CNTT", 5, "Available", 50000);
        book.addBook();

        BookReader reader = new BookReader("10023003", "Nam", "0123456789", 20);
        reader.borrowBook();

        Borrow tran = new Borrow("MKL2", reader.getID(), book.getBookID(), "2025-07-13", "2025-07-20");
        tran.createTransaction();
    }
}

class SystemUser{
    private String userName;
    private String passWord;
    public SystemUser(String userName,String passWord){
        this.userName = userName;
        this.passWord = passWord;
    }
    public boolean login(String tenDangNhap, String matKhau){
        return userName.equals(tenDangNhap) && passWord.equals(matKhau);
    }
}
class Librarian extends SystemUser{
    private String name;
    private String employeeID;
    public Librarian(String userName, String passWord, String name, String employeeID){
        super(userName, passWord);
        this.name = name;
        this.employeeID = employeeID;
    }
    //hien thi ten va employeeID
    public void thongTin(){
        System.out.println("Ten: " + name + "\nID: " + employeeID);
    }
    public void qliSach(){
        System.out.println("Quan li sach ... ");
        //code goes here
    }
    public void qliNguoiDoc(){
        System.out.println("Quan li nguoi doc ");
        //code goes here
    }
}

class Book{
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
}

class BookReader{
    private String readerID;
    private String name;
    private String pn; // sdt
    private int age;
    public BookReader(String readerID, String name, String pn, int age){
        this.readerID = readerID;
        this.name = name;
        this.pn = pn;
        this.age = age;
    }
    public void borrowBook(){
        System.out.println(this.name + " borrowed book");
    }
    public void returnBook(){
        System.out.println(this.name + " returned book");
    }
    public void edit(){
        System.out.println("Information's changed successfully");
    }
    public String getID(){
        return readerID;
    }
    public String getName(){
        return name;
    }
    public String getPn(){
        return pn;
    }
    public int getAge(){
        return age;
    }
}

class Borrow{
    private String transactionID;
    private String readerID;
    private String bookID;
    private String borrowDate;
    private String returnDate;
    public Borrow(String transactionID, String readerID, String bookID, String borrowDate, String returnDate){
        this.transactionID = transactionID;
        this.readerID = readerID;
        this.bookID = bookID;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
    }
    public void createTransaction(){
        System.out.println("Transaction is created");
    }
    public void closeTransaction(){
        System.out.println("Transaction is closed");
    }
    public String getTransactionID(){return transactionID;}
    public String getReaderID(){return readerID;}
    public String getBookID(){return bookID;}
    public String getBorrowDate(){return borrowDate;}
    public String getReturnDate(){return returnDate;}
}
