package pure_java_project.src;

public class User {
    private String userID;
    private String userTen;
    private String userEmail;
    private String userPhoneNumber;

    //default constructor
    public User(){
        setUser("0", "zero", "0@gmail.com", "000");
    }

    public User(String id, String ten, String email, String pn){
        setUser(id, ten, email, pn);
    }

    //setter
    public User setID(String id){this.userID = id; return this;}
    public User setTen(String ten){this.userTen = ten; return this;}
    public User setEmail(String email){this.userEmail = email; return this;}
    public User setPhoneNumber(String pn){this.userPhoneNumber = pn; return this;}

    User setUser(String id, String ten, String email, String pn){
        return this.setID(id).setTen(ten).setEmail(email).setPhoneNumber(pn);
    }

    //getter
    public String getTD(){return this.userID;}
    public String getTen(){return this.userTen;}
    public String getEmail(){return this.userEmail;}
    public String getPhoneNumber(){return this.userPhoneNumber;}

}
