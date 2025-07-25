package UserInLibrary;

import java.io.Serializable;

public class User implements Serializable{
    private String userName;
    private String passWord;
    public User(){
        this.userName = "0";
        this.passWord = "0";
    }
    public User(String userName, String passWord){
        this.userName = userName;
        this.passWord = passWord;
    }
    public void login(){}
    public String getUserName(){return userName;}
    public String getpassWord(){return passWord;}
    public void setUserName(String userName){this.userName = userName;}
    public void setPassWord(String passWord){this.passWord = passWord;}
    public String toString(){
        return "userName: " + userName
        +"\npassWord: " + passWord;
    }
}
