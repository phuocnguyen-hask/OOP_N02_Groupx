package LibSys;
import UserInLibrary.User;
import java.io.*;
import java.util.*;

public class AccountManager {
    private ArrayList<User> users;
    public AccountManager(){
        try{
            FileInputStream fis = new FileInputStream("./database/users.obj");
            ObjectInputStream ois = new ObjectInputStream(fis);
            users = (ArrayList<User>) ois.readObject();
            ois.close();
        } catch (Exception ex){
            users = new ArrayList<User>();
            ex.printStackTrace();
        }
    }
    public void saveUsers(){
        try{
            FileOutputStream fos = new FileOutputStream(new File("./database/users.obj"));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(users);

            oos.close();
        } catch (Exception exception){
            exception.printStackTrace();
        }
    }
    public ArrayList<User> getUsers(){
        return users;
    }
    public void deleteUser(User u){
        users.remove(u);
        saveUsers();
    }
    public void addUser(User u){
        users.add(u);
        saveUsers();
    }
}
