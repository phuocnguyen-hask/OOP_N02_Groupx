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