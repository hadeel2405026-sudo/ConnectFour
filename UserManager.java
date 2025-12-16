package connectfour;
public interface UserManager {

    boolean login(String username, String password);

    boolean register(String username, String password);

    boolean userExists(String username);

}