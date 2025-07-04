
public class UserTest {
    public static void main(String args[]){
        User firstUser = new User();
        //print default in4
        System.out.println("default id: " + firstUser.getTD());
        System.out.println("default ten: " + firstUser.getTen());
        System.out.println("default email: " + firstUser.getEmail());
        System.out.println("default pn: " + firstUser.getPhoneNumber());

        //set in4
        User secondUser = new User("123123", "hello, world!", "meowmeow@gmail.com", "09-232-849");

        System.out.println("\n\n\n");

        //print in4 me set
        System.out.println("default id: " + secondUser.getTD());
        System.out.println("default ten: " + secondUser.getTen());
        System.out.println("default email: " + secondUser.getEmail());
        System.out.println("default pn: " + secondUser.getPhoneNumber());
    }
}
