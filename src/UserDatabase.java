import java.io.*;
import java.util.*;

public class UserDatabase {
    private ArrayList<User> users;

    public UserDatabase() {
        loadUsersFromFile();  // Load once at startup
    }

    // ----------- Public Methods -----------

    public ArrayList<User> getAllUsers() {
        return users;
    }

    public void addUser(User user) {
        users.add(user);
        saveUsersToFile();
    }

    public void addUsers(ArrayList<User> manyUsers) {
        users.addAll(manyUsers);
        saveUsersToFile();
    }

    public void removeUserById(int id) {
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (user.getId() == id) {
                iterator.remove();
                saveUsersToFile();
                return;
            }
        }
    }

    public void removeUser(User user) {
        users.remove(user);
        saveUsersToFile();
    }

    public User findByUsername(String username) {
        for (User user : users) {
            if (user.username().equals(username)) {
                return user;
            }
        }
        return null;
    }
    

    public User findById(int id) {
        for (User user : users) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }

    public void showAllUsers() {
        for (User user : users) {
            System.out.println("ID: " + user.getId());
            System.out.println("Username: " + user.username());
            System.out.println("Password: " + user.passWord());
            System.out.println("-------------");
        }
    }

    public void reloadUsersFromFile() {
        loadUsersFromFile();
    }

    // ----------- Private I/O Methods -----------

    private void loadUsersFromFile() {
        File file = new File("database/users.obj");
        if (file.exists() && file.length() != 0) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                users = (ArrayList<User>) ois.readObject();
                System.out.println("Users loaded from: " + file.getAbsolutePath());
            } catch (Exception ex) {
                users = new ArrayList<>();
                System.err.println("Failed to load users. Starting with empty list.");
                ex.printStackTrace();
            }
        } else {
            users = new ArrayList<>();
            System.out.println("No existing user data. Starting fresh.");
        }
    }

    private void saveUsersToFile() {
        File file = new File("database/users.obj");
        file.getParentFile().mkdirs();  // Ensure folder exists

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(users);
            System.out.println("Users saved to: " + file.getAbsolutePath());
        } catch (Exception ex) {
            System.err.println("Error saving users.");
            ex.printStackTrace();
        }
    }
}

