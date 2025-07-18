public class SystemUser{
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
