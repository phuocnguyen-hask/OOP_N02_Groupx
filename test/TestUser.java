import UserInLibrary.User;
import LibSys.AccountManager;
import java.util.*;
public class TestUser {
    public static void main(String[] args){
        User user = new User("24103624@st.phenikaa-uni.edu.vn", "Meomeomeo123!");
        AccountManager am = new AccountManager();
        am.addUser(user);
        ArrayList<User> users = am.getUsers();
        for (User u : users){
            System.out.println(u.toString());
        }
    }
}
