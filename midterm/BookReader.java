public class BookReader {
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
