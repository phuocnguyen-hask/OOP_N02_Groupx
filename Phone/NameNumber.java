public class NameNumber {
    private String lastName; //tên
    private String telNumber; //số điện thoại
    public NameNumber() {} //default constructor
    //constructor
    public NameNumber(String name, String num) {
        lastName = name;
        telNumber = num;
    }
    // phương thức
    public String getLastName()   {return lastName;}
    public String getTelNumber() {return telNumber;}
}
